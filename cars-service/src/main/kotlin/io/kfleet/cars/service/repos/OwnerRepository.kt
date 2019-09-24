package io.kfleet.cars.service.repos

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.commands.CommandResponse
import mu.KotlinLogging
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.state.ReadOnlyWindowStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.time.Duration
import java.time.Instant
import java.util.*


private val log = KotlinLogging.logger {}

interface IOwnerRepository {
    fun submitCreateOwnerCommand(ownerId: String, ownerName: String): Mono<CreateOwnerCommand>
    fun findCommandResponse(commandId: String): Mono<CommandResponse>
    fun findOwnerByid(ownerId: String): Mono<Owner>
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


    override fun submitCreateOwnerCommand(ownerId: String, ownerName: String): Mono<CreateOwnerCommand> {

        val commandId = UUID.randomUUID().toString()
        val ownerCommand = CreateOwnerCommand(commandId, ownerId, ownerName)

        val msg = MessageBuilder
                .withPayload(ownerCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, ownerCommand.getOwnerId())
                .build()

        return try {
            // this works because cloud stream is configured sync for this topic
            if (outputOwnerCommands.send(msg)) Mono.just(ownerCommand) else Mono.error(RuntimeException("CreateOwnerCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    override fun findCommandResponse(commandId: String): Mono<CommandResponse> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE, commandId, StringSerializer())
        log.debug { "findCommandResponse $commandId -> hostInfo: $hostInfo" }

        return Mono.defer {
            if (hostInfo == interactiveQueryService.currentHostInfo) {
                findCommandResponseLocal(commandId)
            } else {
                // FIXME remote call
                log.debug { "search remote for command response with id $commandId" }
                Mono.empty<CommandResponse>()
            }
        }
    }

    override fun findOwnerByid(ownerId: String): Mono<Owner> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_STORE, ownerId, StringSerializer())
        log.debug { "findOwnerByid $ownerId -> hostInfo: $hostInfo" }

        return Mono.defer {
            if (hostInfo == interactiveQueryService.currentHostInfo) {
                findByIdLocal(ownerId)
            } else {
                // FIXME remote call
                log.debug { "search remote for owner with id $ownerId" }
                Mono.empty<Owner>()
            }
        }
    }

    fun findByIdLocal(ownerId: String): Mono<Owner> {
        return ownerStore().get(ownerId)?.toMono() ?: Mono.error(Exception("owner with id: $ownerId not found"))
    }

    private fun ownerStore(): ReadOnlyKeyValueStore<String, Owner> = interactiveQueryService
            .getQueryableStore(OwnerCommandsProcessorBinding.OWNER_STORE, QueryableStoreTypes.keyValueStore<String, Owner>())


    fun findCommandResponseLocal(commandId: String): Mono<CommandResponse> {
        
        val timeTo = Instant.now()
        val timeFrom = timeTo.minusMillis(Duration.ofHours(1).toMillis())

        commandResponseStore().fetch(commandId, timeFrom.toEpochMilli(), timeTo.toEpochMilli()).use {
            it.asSequence().forEach { kv ->
                return Mono.just(kv.value)
            }
        }

        return Mono.error(Exception("command response with id: $commandId not found"))
    }

    private fun commandResponseStore(): ReadOnlyWindowStore<String, CommandResponse> = interactiveQueryService
            .getQueryableStore(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE, QueryableStoreTypes.windowStore<String, CommandResponse>())

}
