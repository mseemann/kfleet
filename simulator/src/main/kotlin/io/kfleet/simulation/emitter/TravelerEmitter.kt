package io.kfleet.simulation.emitter

import io.kfleet.domain.Traveler
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux

@EnableBinding(TravelerBindings::class)
class TravelerEmitter {

    @StreamEmitter
    @Output(TravelerBindings.TRAVELERS)
    fun emitTravelers(): Flux<Message<Traveler>> = Flux.range(1, TRAVELER_COUNT).map {
        val traveler = Traveler.create(it)
        println("emit: $traveler")
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
