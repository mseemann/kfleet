package io.kfleet.domain.events

import io.kfleet.owner.service.events.*
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue

fun ownerCreatedEvent(buildOwnerCreated: OwnerCreatedEvent.Builder.() -> Unit): OwnerCreatedEvent =
        OwnerCreatedEvent.newBuilder().apply { buildOwnerCreated() }.build()

fun OwnerCreatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}

fun ownerUpdatedEvent(buildOwnerUpdates: OwnerUpdatedEvent.Builder.() -> Unit): OwnerUpdatedEvent =
        OwnerUpdatedEvent.newBuilder().apply { buildOwnerUpdates() }.build()

fun OwnerUpdatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}

fun ownerDeletedEvent(buildOwnerDeleted: OwnerDeletedEvent.Builder.() -> Unit): OwnerDeletedEvent =
        OwnerDeletedEvent.newBuilder().apply { buildOwnerDeleted() }.build()

fun OwnerDeletedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
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
