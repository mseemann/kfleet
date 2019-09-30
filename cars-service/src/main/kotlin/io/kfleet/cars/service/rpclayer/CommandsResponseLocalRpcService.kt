package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.repos.CommandResponseLocalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.scheduler.Schedulers


const val COMMAND_RESPONSE_RPC = "command-response-rpc"

@RestController
@RequestMapping("/$COMMAND_RESPONSE_RPC")
class CommandsResponseLocalRpcService(@Autowired private val commandResponseRepository: CommandResponseLocalRepository) {

    @GetMapping("/{commandId}")
    fun ownerById(@PathVariable("commandId") commandId: String) = commandResponseRepository
            .findByIdLocal(commandId)
            .subscribeOn(Schedulers.elastic())

}
