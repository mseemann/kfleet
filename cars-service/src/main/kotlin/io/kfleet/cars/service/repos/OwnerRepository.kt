package io.kfleet.cars.service.repos

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.cars.service.rpclayer.OWNER_RPC
import io.kfleet.common.createWebClient
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

data class CreateOwnerParams(val ownerId: String, val ownerName: String)

interface IOwnerRepository {
    fun submitCreateOwnerCommand(createOwnerParams: CreateOwnerParams): Mono<CreateOwnerCommand>
    fun findById(ownerId: String): Mono<Owner>
}

interface OwnersBindings {

    companion object {
        const val OWNER_COMMANDS = "owner_commands_out"
    }

    @Output(OWNER_COMMANDS)
    fun ownersCommands(): MessageChannel

}

@Repository
@EnableBinding(OwnersBindings::class)
class OwnerRepository(
        @Autowired @Output(OwnersBindings.OWNER_COMMANDS) val outputOwnerCommands: MessageChannel,
        @Autowired val interactiveQueryService: InteractiveQueryService) : IOwnerRepository {


    override fun submitCreateOwnerCommand(createOwnerParams: CreateOwnerParams): Mono<CreateOwnerCommand> {

        val commandId = UUID.randomUUID().toString()
        val ownerCommand = CreateOwnerCommand(commandId, createOwnerParams.ownerId, createOwnerParams.ownerName)

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

    override fun findById(ownerId: String): Mono<Owner> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_STORE, ownerId, StringSerializer())
        return createWebClient(hostInfo).get().uri("/$OWNER_RPC/$ownerId").retrieve().bodyToMono(Owner::class.java)
    }

}
