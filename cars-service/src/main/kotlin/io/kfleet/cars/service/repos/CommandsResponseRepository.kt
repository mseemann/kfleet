package io.kfleet.cars.service.repos

import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.cars.service.rpclayer.RPC_COMMAND_RESPONSE
import io.kfleet.commands.CommandResponse
import io.kfleet.common.createWebClient
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class CommandsResponseRepository(private val interactiveQueryService: InteractiveQueryService) {

    fun findCommandResponse(commandId: String): Mono<CommandResponse> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE, commandId, StringSerializer())
        return createWebClient(hostInfo).get().uri("$RPC_COMMAND_RESPONSE/$commandId").retrieve().bodyToMono(CommandResponse::class.java)
    }
}
