package io.kfleet.dispatcher.configuration

import io.kfleet.domain.Car
import org.apache.kafka.streams.kstream.KTable
import org.springframework.cloud.stream.annotation.Input

interface CarBinding {
    @Input("cars")
    fun inputCars(): KTable<String, Car>
}
