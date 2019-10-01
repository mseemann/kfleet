package io.kfleet.cars.service.web

import io.kfleet.cars.service.repos.CarsRepository
import io.kfleet.common.customRetry
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

private val log = KotlinLogging.logger {}

@Component
class CarsService(private val carsRepository: CarsRepository) {

    fun cars(request: ServerRequest): Mono<ServerResponse> {
        return carsRepository
                .findAllCars()
                .collectList()
                .customRetry()
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .subscribeOn(Schedulers.elastic())
    }

    fun carById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")
        return carsRepository
                .findById(id)
                .customRetry()
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .onErrorResume { ServerResponse.notFound().build() }
                .subscribeOn(Schedulers.elastic())
    }


    fun carsStateCount(request: ServerRequest): Mono<ServerResponse> {
        return carsRepository.getCarsStateCounts()
                .customRetry()
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .subscribeOn(Schedulers.elastic())
    }

}
