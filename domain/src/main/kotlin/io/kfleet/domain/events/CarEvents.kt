package io.kfleet.domain.events

import io.kfleet.owner.service.events.CarDeregisteredEvent
import io.kfleet.owner.service.events.CarRegisteredEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue

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
