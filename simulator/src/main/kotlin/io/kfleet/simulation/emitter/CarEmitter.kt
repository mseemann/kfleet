package io.kfleet.simulation.emitter

import io.kfleet.domain.Car
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.FluxSender
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.annotation.Async

@EnableBinding(CarBindings::class)
class CarEmitter {


    @Async
    @StreamEmitter
    @Output(CarBindings.CARS)
    fun emitCars(sender: FluxSender) {
        sender.send(randomDelayFluxer(CAR_COUNT).map {
            val car = Car.create(it)
            println("emit: $car")
            MessageBuilder.createMessage(car, headers(it))
        })
    }

}

interface CarBindings {

    companion object {
        const val CARS = "cars"
    }

    @Output(CARS)
    fun cars(): MessageChannel

}
