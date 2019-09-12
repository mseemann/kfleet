package io.kfleet.cars.service.domain

import io.kfleet.domain.GeoPosition
import kotlin.random.Random


data class Car(
        val id: String?,
        val state: CarState = CarState.OUT_OF_POOL,
        val geoPosition: GeoPosition?,
        val stateOfCharge: Double
) {
    companion object {

        fun create(id: Int) = Car(
                id = "$id",
                geoPosition = GeoPosition.random(),
                stateOfCharge = Random.nextDouble(0.0, 100.0),
                state = CarState.values()[Random.nextInt(CarState.values().size)]
        )
    }

    // a Tesla with 70kWh is able to go round about 440km
    fun canReach() = stateOfCharge / 100 * 440
}
