package io.kfleet.traveler.service.repos

import io.kfleet.commands.CommandResponse
import io.kfleet.traveler.service.configuration.TRAVELER_COMMANDS_RESPONSE_STORE
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


@Repository
class CommandsResponseLocalRepository(private val interactiveQueryService: InteractiveQueryService) {


    fun findByIdLocal(commandId: String): Mono<CommandResponse> {
        return commandResponseStore().get(commandId)?.toMono()
                ?: Mono.error(Exception("command response with id: $commandId not found"))
    }

    private fun commandResponseStore(): ReadOnlyKeyValueStore<String, CommandResponse> = interactiveQueryService
            .getQueryableStore(TRAVELER_COMMANDS_RESPONSE_STORE, QueryableStoreTypes.keyValueStore<String, CommandResponse>())

}
