package io.kfleet.cars.service.domain

import io.kfleet.domain.GeoPosition
import kotlin.random.Random


data class Car(
        val id: String,
        val state: CarStatus = CarStatus.IN_USE,
        val geoPosition: GeoPosition,
        val stateOfCharge: Double
) {
    companion object {

        private fun randomStateOfCharge() = Random.nextDouble(0.0, 100.0)

        fun create(id: Int) = Car(
                id = "$id",
                geoPosition = GeoPosition.random(),
                stateOfCharge = randomStateOfCharge(),
                state = CarStatus.values()[Random.nextInt(CarStatus.values().size)]
        )
    }

    // a Tesla with 70kWh is able to go round about 440km
    fun canReach() = stateOfCharge / 100 * 440
}
