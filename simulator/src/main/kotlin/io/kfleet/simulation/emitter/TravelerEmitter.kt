package io.kfleet.simulation.emitter

import io.kfleet.common.headers
import io.kfleet.domain.Traveler
import mu.KotlinLogging
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

const val TRAVELER_COUNT = 20

@EnableBinding(TravelerBindings::class)
class TravelerEmitter {

    @StreamEmitter
    @Output(TravelerBindings.TRAVELERS)
    fun emitTravelers(): Flux<Message<Traveler>> = Flux.range(1, TRAVELER_COUNT).map {
        val traveler = Traveler.create(it)
        logger.debug { "emit: $traveler" }
        MessageBuilder.createMessage(traveler, headers(it))
    }
}

interface TravelerBindings {

    companion object {
        const val TRAVELERS = "travelers"
    }

    @Output(TRAVELERS)
    fun travelers(): MessageChannel

}
