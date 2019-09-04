package io.kfleet.simulation.emitter

import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import reactor.core.publisher.Flux
import java.time.Duration
import kotlin.random.Random

const val CAR_COUNT = 10
const val TRAVELER_COUNT = 20

fun randomDelayFluxer(maxId: Int, sleepFrom: Long = 1, sleepUntil: Long = 5): Flux<Int> = Flux.generate { sink ->
    sink.next(Random.nextInt(1, maxId + 1))
    Thread.sleep(Duration.ofSeconds(Random.nextLong(sleepFrom, sleepUntil)).toMillis())
}

fun headers(id: Int) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to "$id"))
