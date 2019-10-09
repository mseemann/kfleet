package io.kfleet.domain.events

import io.kfleet.cars.service.events.OwnerCreatedEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue

inline fun ownerCreated(buildOwnerCreated: OwnerCreatedEvent.Builder.() -> Unit): OwnerCreatedEvent {
    val builder = OwnerCreatedEvent.newBuilder()
    builder.buildOwnerCreated()
    return builder.build()
}

fun OwnerCreatedEvent.asKeyValue(): KeyValue<String, SpecificRecord> {
    return KeyValue(this.getOwnerId(), this)
}
