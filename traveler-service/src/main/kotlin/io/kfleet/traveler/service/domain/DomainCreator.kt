package io.kfleet.traveler.service.domain


import kotlin.random.Random


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
