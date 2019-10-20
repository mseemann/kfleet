package io.kfleet.domain.events


import io.kfleet.domain.events.traveler.TravelerCreatedEvent
import io.kfleet.domain.events.traveler.TravelerDeletedEvent
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import kotlin.reflect.full.findAnnotation

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class TravelerEvent


fun SpecificRecord.isTravelerEvent(): Boolean {
    return this::class.findAnnotation<TravelerEvent>() != null
}

fun travelerCreatedEvent(buildTravelerCreated: TravelerCreatedEvent.Builder.() -> Unit): TravelerCreatedEvent =
        TravelerCreatedEvent.newBuilder().apply { buildTravelerCreated() }.build()

fun TravelerCreatedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getTravelerId(), this)
}

fun travelerDeletedEvent(buildTravelerDeleted: TravelerDeletedEvent.Builder.() -> Unit): TravelerDeletedEvent =
        TravelerDeletedEvent.newBuilder().apply { buildTravelerDeleted() }.build()

fun TravelerDeletedEvent.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getTravelerId(), this)
}
