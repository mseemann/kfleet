package io.kfleet.traveler.service.rpclayer


import io.kfleet.traveler.service.repos.TravelerLocalRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


@Component
class TravelerLocalRpcService(private val travelerRepository: TravelerLocalRepository) {

    fun travelerById(request: ServerRequest): Mono<ServerResponse> {
        val travelerId = request.pathVariable("travelerId")
        return travelerRepository
                .findByIdLocal(travelerId)
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .onErrorResume { ServerResponse.notFound().build() }
                .subscribeOn(Schedulers.elastic())
    }


}
