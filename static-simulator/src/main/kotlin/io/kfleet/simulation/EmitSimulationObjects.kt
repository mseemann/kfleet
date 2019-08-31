package io.kfleet.simulation

import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux


@EnableBinding(Simulations::class, Sink::class)
class EmitRiders {

    companion object {
        const val RIDERS_COUNT = 20
        const val CAR_COUNT = 10
    }

    data class Rider(val id: String, val name: String)
    data class Car(val id: String)

    @StreamEmitter
    @Output(Simulations.RIDERS)
    fun emitRiders() = Flux.range(1, RIDERS_COUNT).map {
        val rider = Rider(id = "$it", name = "Hello World $it")
        MessageBuilder.createMessage(rider, headers(rider.id))
    }

    @StreamEmitter
    @Output(Simulations.CARS)
    fun emitCars() = Flux.range(1, CAR_COUNT).map {
        val car = Car(id = "$it")
        MessageBuilder.createMessage(car, headers(car.id))
    }

    private fun headers(id: String) = MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to id))

    @StreamListener(Sink.INPUT)
    fun onInput(rider: Rider, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) partition: Int) =
        println("Received: $rider on partition: $partition")
}

interface Simulations {
    companion object {
        const val RIDERS = "riders"
        const val CARS = "cars"
    }

    @Output(RIDERS)
    fun outputRiders(): MessageChannel


    @Output(CARS)
    fun outputCars(): MessageChannel
}
