package io.kfleet.cars.service.web

import customRetry
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.repos.ICarsRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/cars")
class CarsService(@Autowired val carsRepository: ICarsRepository) {

    @GetMapping()
    fun cars(): Flux<Car> = carsRepository.findAllCars().customRetry()

    @GetMapping("/{id}")
    fun carById(@PathVariable("id") id: String): Mono<ResponseEntity<Car>> = carsRepository
            .findById(id)
            .customRetry()
            .map { ResponseEntity(it, HttpStatus.OK) }
            .onErrorResume { Mono.just(ResponseEntity(HttpStatus.NOT_FOUND)) }

    @GetMapping("/stats")
    fun carsStateCount(): Mono<Map<String, Long>> = carsRepository.getCarsStateCounts()
            .customRetry()

}
