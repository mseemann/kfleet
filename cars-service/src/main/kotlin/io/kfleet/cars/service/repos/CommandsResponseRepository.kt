package io.kfleet.cars.service.repos

import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.cars.service.rpclayer.COMMAND_RESPONSE_RPC
import io.kfleet.commands.CommandResponse
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


interface ICommandsResponseRepositroy {
    fun findCommandResponse(commandId: String): Mono<CommandResponse>
}

@Repository
class CommandsResponseRepository(@Autowired val interactiveQueryService: InteractiveQueryService) : ICommandsResponseRepositroy {

    override fun findCommandResponse(commandId: String): Mono<CommandResponse> {
        val hostInfo = interactiveQueryService.getHostInfo(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE, commandId, StringSerializer())
        val webClient = WebClient.create("http://${hostInfo.host()}:${hostInfo.port()}")
        return webClient.get().uri("/$COMMAND_RESPONSE_RPC/$commandId").retrieve().bodyToMono(CommandResponse::class.java)
    }
}
