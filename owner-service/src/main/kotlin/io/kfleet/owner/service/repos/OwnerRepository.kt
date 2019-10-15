package io.kfleet.owner.service.repos

import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.common.WebClientUtil
import io.kfleet.owner.service.commands.CreateOwnerCommand
import io.kfleet.owner.service.commands.DeleteOwnerCommand
import io.kfleet.owner.service.commands.UpdateOwnerNameCommand
import io.kfleet.owner.service.domain.Owner
import io.kfleet.owner.service.domain.createOwnerCommand
import io.kfleet.owner.service.domain.deleteOwnerCommand
import io.kfleet.owner.service.domain.updateOwnerNameCommand
import io.kfleet.owner.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.owner.service.rpclayer.RPC_OWNER
import io.kfleet.owner.service.web.NewCar
import mu.KotlinLogging
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

interface OwnerParams {
    val ownerId: String
}

data class CreateOwnerParams(override val ownerId: String, val ownerName: String) : OwnerParams
data class UpdateOwnerParams(override val ownerId: String, val ownerName: String) : OwnerParams
data class DeleteOwnerParams(override val ownerId: String) : OwnerParams
data class RegisterCarParams(override val ownerId: String, val newCar: NewCar) : OwnerParams
data class DeregisterCarParams(override val ownerId: String, val carId: String) : OwnerParams

private val log = KotlinLogging.logger {}

interface OwnersBindings {

    companion object {
        const val OWNER_COMMANDS = "owner_commands_out"
        const val OWNER_EVENTS = "owner_events_in"
    }

    @Output(OWNER_COMMANDS)
    fun ownersCommands(): MessageChannel

    @Input(OWNER_EVENTS)
    fun ownerEvents(): SubscribableChannel
}

@Component
@EnableBinding(OwnersBindings::class)
class OwnerRepository(
        @Output(OwnersBindings.OWNER_COMMANDS) private val outputOwnerCommands: MessageChannel,
        private val interactiveQueryService: InteractiveQueryService,
        private val webClientUtil: WebClientUtil) {

    fun submitCreateOwnerCommand(createOwnerParams: CreateOwnerParams): Mono<CreateOwnerCommand> {

        val ownerCommand = createOwnerCommand {
            commandId = UUID.randomUUID().toString()
            ownerId = createOwnerParams.ownerId
            name = createOwnerParams.ownerName
        }

        val msg = MessageBuilder
                .withPayload(ownerCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, ownerCommand.getOwnerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputOwnerCommands.send(msg)) Mono.just(ownerCommand) else Mono.error(RuntimeException("CreateOwnerCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    fun submitUpdateOwnerNameCommand(updateOwnerParams: UpdateOwnerParams): Mono<UpdateOwnerNameCommand> {
        val ownerCommand = updateOwnerNameCommand {
            commandId = UUID.randomUUID().toString()
            ownerId = updateOwnerParams.ownerId
            name = updateOwnerParams.ownerName
        }

        val msg = MessageBuilder
                .withPayload(ownerCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, ownerCommand.getOwnerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputOwnerCommands.send(msg)) Mono.just(ownerCommand) else Mono.error(RuntimeException("UpdateOwnerNameCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    fun submitDeleteOwnerCommand(deleteOwnerParams: DeleteOwnerParams): Mono<DeleteOwnerCommand> {
        val ownerCommand = deleteOwnerCommand {
            commandId = UUID.randomUUID().toString()
            ownerId = deleteOwnerParams.ownerId
        }

        val msg = MessageBuilder
                .withPayload(ownerCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, ownerCommand.getOwnerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputOwnerCommands.send(msg)) Mono.just(ownerCommand) else Mono.error(RuntimeException("DeleteOwnerCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    fun findById(ownerId: String): Mono<Owner> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_RW_STORE, ownerId, StringSerializer())
        return webClientUtil.doGet(hostInfo, "$RPC_OWNER/$ownerId", Owner::class.java)
    }

    @StreamListener(OwnersBindings.OWNER_EVENTS)
    fun process(message: Message<OwnerCreatedEvent>) {
        log.debug { "owner event: $message" }
    }
}
