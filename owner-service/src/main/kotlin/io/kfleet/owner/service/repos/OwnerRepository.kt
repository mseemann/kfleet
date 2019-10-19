package io.kfleet.owner.service.repos

import io.kfleet.common.WebClientUtil
import io.kfleet.domain.events.owner.OwnerCreatedEvent
import io.kfleet.owner.service.commands.*
import io.kfleet.owner.service.configuration.StoreNames
import io.kfleet.owner.service.configuration.TopicBindingNames
import io.kfleet.owner.service.domain.*
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

    @Output(TopicBindingNames.OWNER_COMMANDS_OUT)
    fun ownersCommands(): MessageChannel

    @Input(TopicBindingNames.OWNER_EVENTS_IN)
    fun ownerEvents(): SubscribableChannel
}

@Component
@EnableBinding(OwnersBindings::class)
class OwnerRepository(
        @Output(TopicBindingNames.OWNER_COMMANDS_OUT) private val outputOwnerCommands: MessageChannel,
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

    fun submitRegisterCarCommand(regsiterCarParams: RegisterCarParams): Mono<RegisterCarCommand> {
        val registerCarCommand = registerCarCommand {
            commandId = UUID.randomUUID().toString()
            ownerId = regsiterCarParams.ownerId
            type = regsiterCarParams.newCar.model
        }

        val msg = MessageBuilder
                .withPayload(registerCarCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, registerCarCommand.getOwnerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputOwnerCommands.send(msg)) Mono.just(registerCarCommand) else Mono.error(RuntimeException("RegisterCarCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    fun submitDeregisterCarCommand(deregisterCarParams: DeregisterCarParams): Mono<DeregisterCarCommand> {

        val deregisterCarCommand = deregisterCarCommand {
            commandId = UUID.randomUUID().toString()
            ownerId = deregisterCarParams.ownerId
            carId = deregisterCarParams.carId
        }

        val msg = MessageBuilder
                .withPayload(deregisterCarCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, deregisterCarCommand.getOwnerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputOwnerCommands.send(msg)) Mono.just(deregisterCarCommand) else Mono.error(RuntimeException("DeregisterCarCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    fun findById(ownerId: String): Mono<Owner> {
        val hostInfo = interactiveQueryService.getHostInfo(StoreNames.OWNER_RW_STORE, ownerId, StringSerializer())
        return webClientUtil.doGet(hostInfo, "$RPC_OWNER/$ownerId", Owner::class.java)
    }

    // just an example: how to listen to a topic
    @StreamListener(TopicBindingNames.OWNER_EVENTS_IN)
    fun process(message: Message<OwnerCreatedEvent>) {
        log.debug { "owner event: $message" }
    }
}
