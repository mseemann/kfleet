package io.kfleet.traveler.service.repos

import io.kfleet.commands.CommandResponse
import io.kfleet.common.WebClientUtil
import io.kfleet.traveler.service.configuration.TRAVELER_COMMANDS_RESPONSE_STORE
import io.kfleet.traveler.service.rpclayer.RPC_COMMAND_RESPONSE
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class CommandsResponseRepository(
        private val interactiveQueryService: InteractiveQueryService,
        private val webClientUtil: WebClientUtil) {

    fun findCommandResponse(commandId: String): Mono<CommandResponse> {
        val hostInfo = interactiveQueryService.getHostInfo(TRAVELER_COMMANDS_RESPONSE_STORE, commandId, StringSerializer())
        return webClientUtil.doGet(hostInfo, "$RPC_COMMAND_RESPONSE/$commandId", CommandResponse::class.java)
    }
}
