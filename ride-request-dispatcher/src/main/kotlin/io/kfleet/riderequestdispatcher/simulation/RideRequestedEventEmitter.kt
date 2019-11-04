package io.kfleet.riderequestdispatcher.simulation

import io.kfleet.common.headers
import io.kfleet.common.randomDelayFluxer
import io.kfleet.domain.events.GeoPositionFactory
import io.kfleet.domain.events.ride.RideRequestedEvent
import io.kfleet.domain.events.rideRequestedEvent
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
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private val logger = KotlinLogging.logger {}

const val TRAVELER_COUNT = 10

interface RideRequestedEventsOutBindings {

    companion object {
        const val RIDE_REQUEST_EVENTS = "ride_request_events_out"
    }

    @Output(RIDE_REQUEST_EVENTS)
    fun rideRquestedEvents(): MessageChannel

}

@EnableBinding(RideRequestedEventsOutBindings::class)
class RideRequestedEventEmitter {

    @Value("\${cars.service.simulation.events.enabled}")
    val simulationEnabled: Boolean? = null

    @StreamEmitter
    @Output(RideRequestedEventsOutBindings.RIDE_REQUEST_EVENTS)
    fun emitRideRequestEvents(): Flux<Message<RideRequestedEvent>> = if (simulationEnabled == true) randomDelayFluxer(TRAVELER_COUNT, sleepFrom = 10, sleepUntil = 15).map {
        val position = GeoPositionFactory.createRandomRideRequetsedLocation()
        val geoIndex = position.toQuadrantIndex()
        val rideRequestedEvent = rideRequestedEvent {
            travelerId = "$it"
            from = position
            fromGeoIndex = geoIndex
            to = GeoPositionFactory.createRandomRideRequetsedLocation()
            requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                    .withZone(ZoneOffset.UTC)
                    .format(ZonedDateTime.now())
        }
        logger.debug { "emit: $rideRequestedEvent" }
        MessageBuilder.createMessage(rideRequestedEvent, headers(geoIndex))
    } else Flux.empty()
}
