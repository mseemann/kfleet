package io.kfleet.domain

import kotlin.random.Random

data class GeoPosition(val lat: Double, val lng: Double) {

    companion object {

        val OsloLatRange = arrayOf(59.7984951859, 60.0334203198)
        val OsloLngRange = arrayOf(10.3772388202, 10.9805373651)

        fun random() = GeoPosition(
            lat = Random.nextDouble(OsloLatRange[0], OsloLatRange[1]),
            lng = Random.nextDouble(OsloLngRange[0], OsloLngRange[1])
        )
    }
}
