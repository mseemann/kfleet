package io.kfleet.cars.service.domain


import kotlin.random.Random

object CarFactory {

    fun createRandom(id: Int) = Car(
            "$id",
            Random.nextDouble(0.0, 100.0),
            CarState.values()[Random.nextInt(CarState.values().size)],
            GeoPositionFactory.createRandom()
    )

    fun create(
            id: String,
            state: CarState,
            stateOfCharge: Double,
            geoPosition: GeoPosition): Car = Car.newBuilder().apply {
        setId(id)
        setState(state)
        setStateOfCharge(stateOfCharge)
        setGeoPosition(geoPosition)
    }.build()
}

object GeoPositionFactory {

    val OsloLatRange = arrayOf(59.7984951859, 60.0334203198)
    val OsloLngRange = arrayOf(10.3772388202, 10.9805373651)

    fun createRandom() = GeoPosition(
            Random.nextDouble(OsloLatRange[0], OsloLatRange[1]),
            Random.nextDouble(OsloLngRange[0], OsloLngRange[1])
    )

    fun create(lng: Double, lat: Double): GeoPosition = GeoPosition.newBuilder().apply {
        setLng(lng)
        setLat(lat)
    }.build()
}
