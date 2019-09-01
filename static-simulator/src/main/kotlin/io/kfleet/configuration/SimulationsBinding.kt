package io.kfleet.configuration

import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel


interface SimulationsBinding {

    companion object {
        const val TRAVELERS = "travelers"
        const val CARS = "cars"
    }

    @Output(TRAVELERS)
    fun travelers(): MessageChannel

    @Output(CARS)
    fun cars(): MessageChannel
}
