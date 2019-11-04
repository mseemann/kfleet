package io.kfleet.riderequestdispatcher.simulation

import io.kfleet.common.headers
import io.kfleet.common.randomDelayFluxer
import io.kfleet.domain.events.GeoPositionFactory
import io.kfleet.domain.events.car.CarLocationChangedEvent
import io.kfleet.domain.events.carLocationChangedEvent
import io.kfleet.domain.events.toQuadrantIndex
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

interface CarsLocationEventsOutBindings {

    companion object {
        const val CARS_LOCATION = "car_locations_out"
    }

    @Output(CARS_LOCATION)
    fun carsLocationChangedEvents(): MessageChannel

}

@EnableBinding(CarsLocationEventsOutBindings::class)
class CarLocationChangedEventEmitter {

    @Value("\${cars.service.simulation.location.events.enabled}")
    val simulationEnabled: Boolean? = null

    @StreamEmitter
    @Output(CarsLocationEventsOutBindings.CARS_LOCATION)
    fun emitCarLocations(): Flux<Message<CarLocationChangedEvent>> = if (simulationEnabled == true) randomDelayFluxer(CAR_COUNT, sleepFrom = 2, sleepUntil = 5).map {
        val position = GeoPositionFactory.createRandom()
        val carLocationChangedEvent = carLocationChangedEvent {
            carId = "$it"
            geoPosition = position
            geoPositionIndex = position.toQuadrantIndex()
        }
        logger.debug { "emit: $carLocationChangedEvent" }
        MessageBuilder.createMessage(carLocationChangedEvent, headers(it))
    } else Flux.empty()


}
