package io.kfleet.cars.service.events

import io.kfleet.cars.service.domain.CarState

interface CarEvent {
    val id: String
    val type: String
}

data class CarCreatedEvent(
        override val id: String,
        val state: CarState = CarState.OUT_OF_POOL,
        val ownerId: String) : CarEvent {
    override val type: String = "CarCreated"
}
