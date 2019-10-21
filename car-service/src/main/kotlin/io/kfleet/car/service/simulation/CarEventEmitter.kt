package io.kfleet.car.service.simulation

import io.kfleet.common.headers
import io.kfleet.common.randomDelayFluxer
import io.kfleet.domain.events.carDeregisteredEvent
import io.kfleet.domain.events.carRegisteredEvent
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

private const val CAR_COUNT = 10

interface CarEventOutBindings {

    companion object {
        const val CARS = "car_events_out"
    }

    @Output(CARS)
    fun carEvents(): MessageChannel

}

@EnableBinding(CarEventOutBindings::class)
class CarEventEmitter {

    @Value("\${cars.service.simulation.events.enabled}")
    val simulationEnabled: Boolean? = null

    @StreamEmitter
    @Output(CarEventOutBindings.CARS)
    fun emitCarEvents(): Flux<Message<SpecificRecord>> = if (simulationEnabled == true) randomDelayFluxer(CAR_COUNT, sleepUntil = 2).map {

        val carEvent: SpecificRecord =
                if (Random.nextBoolean())
                    carRegisteredEvent { carId = "$it" }
                else
                    carDeregisteredEvent { carId = "$it" }

        logger.debug { "emit: $carEvent" }
        MessageBuilder.createMessage(carEvent, headers(it))
    } else Flux.empty()


}
