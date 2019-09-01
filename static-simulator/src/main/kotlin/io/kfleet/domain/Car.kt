package io.kfleet.domain

import kotlin.random.Random


data class Car(
    val id: String,
    val state: CarState = CarState.IN_USE,
    val geoPosition: GeoPosition,
    val stateOfCharge: Double
) {
    companion object {

        enum class CarState {
            FREE, IN_USE
        }
        
        private fun randomStateOfCharge() = Random.nextDouble(0.0, 100.0)

        fun create(id: Int) = Car(
            id = "$id",
            geoPosition = GeoPosition.random(),
            stateOfCharge = randomStateOfCharge()
        )
    }

    // a Tesla with 70kWh is able to go round about 440km
    fun canReach() = stateOfCharge / 100 * 440;
}
