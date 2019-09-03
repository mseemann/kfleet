package io.kfleet.simulation.emitter

import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import reactor.core.publisher.Flux
import java.time.Duration
import kotlin.random.Random

val CAR_COUNT = 10
val TRAVELER_COUNT = 20
var infiniteCounter = 0L

fun fluxer(maxId: Int, sleepFrom: Long = 1, sleepUntil: Long = 5) = Flux.generate<Int> { sink ->
    infiniteCounter++
    sink.next((infiniteCounter % maxId + 1).toInt())
    Thread.sleep(Duration.ofSeconds(Random.nextLong(sleepFrom, sleepUntil)).toMillis())
}

fun headers(id: Int) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to "$id"))
