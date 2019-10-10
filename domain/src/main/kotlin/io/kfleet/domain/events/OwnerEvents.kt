package io.kfleet.domain.events

import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.events.OwnerDeletedEvent
import io.kfleet.cars.service.events.OwnerUpdatedEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue

fun ownerCreated(buildOwnerCreated: OwnerCreatedEvent.Builder.() -> Unit): OwnerCreatedEvent =
        OwnerCreatedEvent.newBuilder().apply { buildOwnerCreated() }.build()

fun OwnerCreatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}

fun ownerUpdated(buildOwnerUpdates: OwnerUpdatedEvent.Builder.() -> Unit): OwnerUpdatedEvent =
        OwnerUpdatedEvent.newBuilder().apply { buildOwnerUpdates() }.build()

fun OwnerUpdatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}

fun ownerDeleted(buildOwnerDeleted: OwnerDeletedEvent.Builder.() -> Unit): OwnerDeletedEvent =
        OwnerDeletedEvent.newBuilder().apply { buildOwnerDeleted() }.build()

fun OwnerDeletedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getOwnerId(), this)
}
