package io.kfleet.cars.service.events

import io.kfleet.cars.service.domain.CarState

interface Event {
    val id: String
    val type: String
}

data class CarCreated(
        override val id: String,
        val state: CarState = CarState.OUT_OF_POOL,
        val ownerId: String) : Event {
    override val type: String = "CarCreated"
}
