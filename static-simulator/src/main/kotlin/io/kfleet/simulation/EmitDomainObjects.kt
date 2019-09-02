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
import java.time.Duration
import kotlin.random.Random

@EnableBinding(SimulationsBinding::class)
class EmitDomainObjects {

    private val TRAVELER_COUNT = 20
    private val CAR_COUNT = 10
    private var carInfiniteCounter = 0

    @StreamEmitter
    @Output(SimulationsBinding.TRAVELERS)
    fun emitTravelers() = Flux.range(1, TRAVELER_COUNT).map {
        MessageBuilder.createMessage(Traveler.create(it), headers(it))
    }


    @StreamEmitter
    @Output(SimulationsBinding.CARS)
    fun emitCars() = fluxer(CAR_COUNT).map {
        println(it)
        MessageBuilder.createMessage(Car.create(it), headers(it))
    }

    private fun fluxer(maxId: Int, sleepFrom: Long = 1, sleepUntil: Long = 5) = Flux.generate<Int> { sink ->
        sink.next((carInfiniteCounter++) % maxId + 1)
        Thread.sleep(Duration.ofSeconds(Random.nextLong(sleepFrom, sleepUntil)).toMillis())
    }

    private fun headers(id: Int) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to "$id"))
}
