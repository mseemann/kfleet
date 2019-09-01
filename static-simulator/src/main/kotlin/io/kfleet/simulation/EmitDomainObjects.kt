package io.kfleet.simulation

import io.kfleet.configuration.SimulationsBinding
import io.kfleet.domain.Car
import io.kfleet.domain.Traveler
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux

@EnableBinding(SimulationsBinding::class)
class EmitDomainObjects {

    private val TRAVELER_COUNT = 20
    private val CAR_COUNT = 10

    @StreamEmitter
    @Output(SimulationsBinding.TRAVELERS)
    fun emitTraveler() = Flux.range(1, TRAVELER_COUNT).map {
        MessageBuilder.createMessage(Traveler.create(it), headers(it))
    }

    @StreamEmitter
    @Output(SimulationsBinding.CARS)
    fun emitCars() = Flux.range(1, CAR_COUNT).map {
        MessageBuilder.createMessage(Car.create(it), headers(it))
    }

    private fun headers(id: Int) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to "$id"))
}
