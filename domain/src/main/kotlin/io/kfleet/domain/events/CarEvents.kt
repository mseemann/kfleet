package io.kfleet.domain.events

import io.kfleet.domain.events.car.CarDeregisteredEvent
import io.kfleet.domain.events.car.CarLocationChangedEvent
import io.kfleet.domain.events.car.CarRegisteredEvent
import io.kfleet.domain.events.car.GeoPosition
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import kotlin.reflect.full.findAnnotation

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class CarEvent

fun SpecificRecord.isCarEvent(): Boolean {
    return this::class.findAnnotation<CarEvent>() != null
}

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class CarLocationEvent

fun SpecificRecord.isCarLocationEvent(): Boolean {
    return this::class.findAnnotation<CarLocationEvent>() != null
}

fun carRegisteredEvent(buildCarRegistered: CarRegisteredEvent.Builder.() -> Unit): CarRegisteredEvent =
        CarRegisteredEvent.newBuilder().apply { buildCarRegistered() }.build()

fun CarRegisteredEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getCarId(), this)
}

fun carDeregisteredEvent(buildCarDeregistered: CarDeregisteredEvent.Builder.() -> Unit): CarDeregisteredEvent =
        CarDeregisteredEvent.newBuilder().apply { buildCarDeregistered() }.build()

fun CarDeregisteredEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getCarId(), this)
}

fun geoPosition(buildGeoPosition: GeoPosition.Builder.() -> Unit): GeoPosition =
        GeoPosition.newBuilder().apply { buildGeoPosition() }.build()


fun carLocationChangedEvent(buildCarLocationEvent: CarLocationChangedEvent.Builder.() -> Unit): CarLocationChangedEvent =
        CarLocationChangedEvent.newBuilder().apply { buildCarLocationEvent() }.build()

fun CarLocationChangedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getCarId(), this)
}
