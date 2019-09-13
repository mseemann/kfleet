package io.kfleet.cars.service.web

import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import io.kfleet.cars.service.events.CarCreated
import io.kfleet.cars.service.repos.ICarsRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/cars")
class CarsService(@Autowired val carsRepository: ICarsRepository) {
    
    @GetMapping("/")
    fun cars(): Flux<Car> = carsRepository.findAllCars()

    @GetMapping("/{id}")
    fun carById(@PathVariable("id") id: String): Mono<ResponseEntity<Car>> = carsRepository
            .findById(id)
            .map { ResponseEntity(it, HttpStatus.OK) }
            .defaultIfEmpty(ResponseEntity(HttpStatus.NOT_FOUND))

    @GetMapping("/stats")
    fun carsStateCount(): Mono<Map<String, Long>> = carsRepository.getCarsStateCounts()

    @PostMapping("/create/{ownerId}")
    fun createCarForOwner(@PathVariable("ownerId") ownerId: String): String {

        // Command: CreateCarCommand
        // Event: CarCreatedEvent

        // TODO is this a command or an event?
        val event = CarCreated(
                id = UUID.randomUUID().toString(),
                state = CarState.OUT_OF_POOL,
                ownerId = ownerId)

        // TODO how to check that the owner exists - at this place
        // or later if the event is processed?

        logger.debug { event }
        // carsRepository.publishCarEvents(event)
        return event.id
    }
}
