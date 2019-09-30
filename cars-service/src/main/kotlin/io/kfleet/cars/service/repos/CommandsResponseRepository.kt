package io.kfleet.cars.service.repos

import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.cars.service.rpclayer.COMMAND_RESPONSE_RPC
import io.kfleet.commands.CommandResponse
import io.kfleet.common.createWebClient
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class CommandsResponseRepository(@Autowired private val interactiveQueryService: InteractiveQueryService) {

    fun findCommandResponse(commandId: String): Mono<CommandResponse> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE, commandId, StringSerializer())
        return createWebClient(hostInfo).get().uri("/$COMMAND_RESPONSE_RPC/$commandId").retrieve().bodyToMono(CommandResponse::class.java)
    }
}
