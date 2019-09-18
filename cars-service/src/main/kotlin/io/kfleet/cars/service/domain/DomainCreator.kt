package io.kfleet.cars.service.domain


import kotlin.random.Random

object CarCreator {
    fun create(id: Int) = Car(
            "$id",
            Random.nextDouble(0.0, 100.0),
            CarState.values()[Random.nextInt(CarState.values().size)],
            GeoPositionCreator.create()
    )
}

object GeoPositionCreator {

    val OsloLatRange = arrayOf(59.7984951859, 60.0334203198)
    val OsloLngRange = arrayOf(10.3772388202, 10.9805373651)

    fun create() = GeoPosition(
            Random.nextDouble(OsloLatRange[0], OsloLatRange[1]),
            Random.nextDouble(OsloLngRange[0], OsloLngRange[1])
    )
}

//data class Car(
//        val id: String?,
//        val state: CarState = CarState.OUT_OF_POOL,
//        val geoPosition: GeoPosition?,
//        val stateOfCharge: Double
//) {
//    companion object {
//
//        fun create(id: Int) = Car(
//                id = "$id",
//                geoPosition = GeoPosition.random(),
//                stateOfCharge = Random.nextDouble(0.0, 100.0),
//                state = CarState.values()[Random.nextInt(CarState.values().size)]
//        )
//    }
//
//    // a Tesla with 70kWh is able to go round about 440km
//    fun canReach() = stateOfCharge / 100 * 440
//}
