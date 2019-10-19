package io.kfleet.domain.events

import io.kfleet.domain.events.car.CarDeregisteredEvent
import io.kfleet.domain.events.car.CarRegisteredEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class CarEvent

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
