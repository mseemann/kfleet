package io.kfleet.domain.events

import io.kfleet.owner.service.events.OwnerCreatedEvent
import io.kfleet.owner.service.events.OwnerDeletedEvent
import io.kfleet.owner.service.events.OwnerUpdatedEvent
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
