package io.kfleet.owner.service.repos

import io.kfleet.commands.CommandResponse
import io.kfleet.owner.service.configuration.OWNER_COMMANDS_RESPONSE_STORE
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyWindowStore
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.Instant


@Repository
class CommandsResponseLocalRepository(private val interactiveQueryService: InteractiveQueryService) {


    fun findByIdLocal(commandId: String): Mono<CommandResponse> {

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
            .getQueryableStore(OWNER_COMMANDS_RESPONSE_STORE, QueryableStoreTypes.windowStore())

}
