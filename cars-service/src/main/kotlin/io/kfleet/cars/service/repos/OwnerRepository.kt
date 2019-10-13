package io.kfleet.cars.service.repos

import io.kfleet.cars.service.WebClientUtil
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.DeleteOwnerCommand
import io.kfleet.cars.service.commands.UpdateOwnernameCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.createOwnerCommand
import io.kfleet.cars.service.domain.deleteOwnerCommand
import io.kfleet.cars.service.domain.updateOwnerNameCommand
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.cars.service.rpclayer.RPC_OWNER
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

data class CreateOwnerParams(val ownerId: String, val ownerName: String)
data class UpdateOwnerParams(val ownerId: String, val ownerName: String)
data class DeleteOwnerParams(val ownerId: String)

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

    fun submitUpdateOwnerNameCommand(updateOwnerParams: UpdateOwnerParams): Mono<UpdateOwnernameCommand> {
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
