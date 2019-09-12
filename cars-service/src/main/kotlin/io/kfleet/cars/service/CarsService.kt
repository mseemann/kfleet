package io.kfleet.cars.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import io.kfleet.cars.service.events.CarCreated
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/cars")
class CarsService {

    @Autowired
    lateinit var carsRepository: CarsRepository

    @Autowired
    lateinit var mapper: ObjectMapper

    @GetMapping("/")
    fun cars(): Flux<Car> {
        // TODO all is only local all - how to get and mix global all
        return carsRepository.carsStore().all().use {
            it.asSequence().map { kv -> mapper.readValue<Car>(kv.value) }.toList().toFlux()
        }
    }

    @GetMapping("/{id}")
    fun car(@PathVariable("id") id: String): ResponseEntity<Car> {
        // TODO get car by id from a different host - if the car is not on this host
        return carsRepository.carsStore().get(id)?.let {
            ResponseEntity(mapper.readValue<Car>(it), HttpStatus.OK)
        } ?: ResponseEntity(HttpStatus.NOT_FOUND)
    }

    @GetMapping("/stats")
    fun carsStats(): Mono<Map<String, Long>> {
        // TODO all is only local all - how to get and mix global all
        return carsRepository.carStateStore().all().use {
            it.asSequence().map { it.key to it.value }.toMap()
        }.toMono()
    }

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
        carsRepository.publishCarEvents(event)
        return event.id
    }
}
