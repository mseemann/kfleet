package io.kfleet.validator.configuration

import io.kfleet.domain.Car
import org.apache.kafka.streams.kstream.KTable
import org.springframework.cloud.stream.annotation.Input

interface TravelerBinding {
    @Input("travelers")
    fun inputTravelers(): KTable<String, Car>
}
