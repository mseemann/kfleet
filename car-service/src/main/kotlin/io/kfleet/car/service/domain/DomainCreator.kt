package io.kfleet.car.service.domain


import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import io.kfleet.cars.service.domain.GeoPosition
import kotlin.random.Random

fun car(buildCar: Car.Builder.() -> Unit): Car = Car.newBuilder().apply { buildCar() }.build()

object CarFactory {

    fun createRandom(id: Int) = Car(
            "$id",
            Random.nextDouble(0.0, 100.0),
            CarState.values()[Random.nextInt(CarState.values().size)],
            GeoPositionFactory.createRandom()
    )
}

fun geoPosition(buildGeoPosition: GeoPosition.Builder.() -> Unit): GeoPosition =
        GeoPosition.newBuilder().apply { buildGeoPosition() }.build()

object GeoPositionFactory {

    val OsloLatRange = arrayOf(59.7984951859, 60.0334203198)
    val OsloLngRange = arrayOf(10.3772388202, 10.9805373651)

    fun createRandom() = GeoPosition(
            Random.nextDouble(OsloLatRange[0], OsloLatRange[1]),
            Random.nextDouble(OsloLngRange[0], OsloLngRange[1])
    )

}
