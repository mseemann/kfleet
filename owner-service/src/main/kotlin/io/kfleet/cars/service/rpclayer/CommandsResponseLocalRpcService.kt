package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.repos.CommandsResponseLocalRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Component
class CommandsResponseLocalRpcService(
        private val commandResponseRepository: CommandsResponseLocalRepository) {


    fun commandById(request: ServerRequest): Mono<ServerResponse> {
        val commandId = request.pathVariable("commandId")
        return commandResponseRepository
                .findByIdLocal(commandId)
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .onErrorResume { ServerResponse.notFound().build() }
                .subscribeOn(Schedulers.elastic())
    }

}
