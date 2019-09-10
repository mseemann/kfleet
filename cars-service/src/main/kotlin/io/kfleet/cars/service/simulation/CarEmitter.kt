package io.kfleet.cars.service.simulation

import io.kfleet.cars.service.domain.Car
import io.kfleet.common.headers
import io.kfleet.common.randomDelayFluxer
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

const val CAR_COUNT = 10

interface CarsOutBindings {

    companion object {
        const val CARS = "cars_out"
    }

    @Output(CARS)
    fun cars(): MessageChannel

}


@EnableBinding(CarsOutBindings::class)
class CarEmitter {

    @Value("\${cars.service.simulation.enabled}")
    val simulationEnabled: Boolean? = null

    @StreamEmitter
    @Output(CarsOutBindings.CARS)
    fun emitCars(): Flux<Message<Car>> = if (simulationEnabled == true) randomDelayFluxer(CAR_COUNT).map {
        val car = Car.create(it)
        logger.debug { "emit: $car" }
        MessageBuilder.createMessage(car, headers(it))
    } else Flux.empty()


}
