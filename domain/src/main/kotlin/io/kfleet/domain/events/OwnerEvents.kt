package io.kfleet.domain.events

import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.events.OwnerDeletedEvent
import io.kfleet.cars.service.events.OwnerUpdatedEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue

fun ownerCreated(buildOwnerCreated: OwnerCreatedEvent.Builder.() -> Unit): OwnerCreatedEvent {
    val builder = OwnerCreatedEvent.newBuilder()
    builder.buildOwnerCreated()
    return builder.build()
}

fun OwnerCreatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}

fun ownerUpdated(buildOwnerUpdates: OwnerUpdatedEvent.Builder.() -> Unit): OwnerUpdatedEvent {
    val builder = OwnerUpdatedEvent.newBuilder()
    builder.buildOwnerUpdates()
    return builder.build()
}

fun OwnerUpdatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}

fun ownerDeleted(buildOwnerDeleted: OwnerDeletedEvent.Builder.() -> Unit): OwnerDeletedEvent {
    val builder = OwnerDeletedEvent.newBuilder()
    builder.buildOwnerDeleted()
    return builder.build()
}

fun OwnerDeletedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}
