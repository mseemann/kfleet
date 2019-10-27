package io.kfleet.car.service.simulation

import io.kfleet.common.headers
import io.kfleet.common.randomDelayFluxer
import io.kfleet.domain.events.GeoPositionFactory
import io.kfleet.domain.events.car.CarLocationChangedEvent
import io.kfleet.domain.events.carLocationChangedEvent
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


interface CarsLocationEventsOutBindings {

    companion object {
        const val CARS_LOCATION = "cars_location_out"
    }

    @Output(CARS_LOCATION)
    fun cars(): MessageChannel

}

@EnableBinding(CarsLocationEventsOutBindings::class)
class CarLocationChangedEventEmitter {

    @Value("\${cars.service.simulation.location.events.enabled}")
    val simulationEnabled: Boolean? = null

    @StreamEmitter
    @Output(CarsLocationEventsOutBindings.CARS_LOCATION)
    fun emitCarLocations(): Flux<Message<CarLocationChangedEvent>> = if (simulationEnabled == true) randomDelayFluxer(CAR_COUNT, sleepUntil = 2).map {
        val carLocationChangedEvent = carLocationChangedEvent {
            carId = "$it"
            geoPosition = GeoPositionFactory.createRandom()
        }
        logger.debug { "emit: $carLocationChangedEvent" }
        MessageBuilder.createMessage(carLocationChangedEvent, headers(it))
    } else Flux.empty()


}
