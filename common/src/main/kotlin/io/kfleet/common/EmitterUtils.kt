package io.kfleet.common


import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.SynchronousSink
import java.time.Duration
import kotlin.random.Random

fun randomDelayFluxer(maxId: Int, sleepFrom: Long = 1, sleepUntil: Long = 5): Flux<Int> {
    return Flux.generate { sink: SynchronousSink<Int> ->
        sink.next(Random.nextInt(maxId) + 1)
    }.concatMap { id: Int ->
        val delay = Duration.ofSeconds(Random.nextLong(sleepFrom, sleepUntil))
        Mono.delay(delay).map { id }
    }
}

fun headers(id: Int) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to "$id"))
fun headers(id: String) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to id))
