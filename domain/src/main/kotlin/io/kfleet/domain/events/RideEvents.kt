package io.kfleet.domain.events

import io.kfleet.domain.events.ride.GeoPositionRideRequestedEvent
import io.kfleet.domain.events.ride.RideRequestedEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import kotlin.reflect.full.findAnnotation

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class RideEvent

fun SpecificRecord.isRideEvent(): Boolean {
    return this::class.findAnnotation<RideEvent>() != null
}

fun rideRequestedEvent(buildRideRequestedEvent: RideRequestedEvent.Builder.() -> Unit): RideRequestedEvent =
        RideRequestedEvent.newBuilder().apply { buildRideRequestedEvent() }.build()

fun RideRequestedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getFromGeoIndex(), this)
}

fun geoPositionRideRequested(buildGeoPosition: GeoPositionRideRequestedEvent.Builder.() -> Unit): GeoPositionRideRequestedEvent =
        GeoPositionRideRequestedEvent.newBuilder().apply { buildGeoPosition() }.build()


