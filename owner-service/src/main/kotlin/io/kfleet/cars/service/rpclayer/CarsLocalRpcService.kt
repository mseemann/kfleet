package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.repos.CarsLocalRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


@Component
class CarsLocalRpcService(private val carsRepository: CarsLocalRepository) {


    fun cars(request: ServerRequest): Mono<ServerResponse> {
        return carsRepository
                .findAllCarsLocal()
                .collectList()
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .subscribeOn(Schedulers.elastic())
    }


    fun carById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")
        return carsRepository
                .findByIdLocal(id)
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .onErrorResume { ServerResponse.notFound().build() }
                .subscribeOn(Schedulers.elastic())
    }


    fun carsStateCount(request: ServerRequest): Mono<ServerResponse> {
        return carsRepository
                .getLocalCarsStateCounts()
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .subscribeOn(Schedulers.elastic())
    }
}
