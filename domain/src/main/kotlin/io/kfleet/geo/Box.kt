package io.kfleet.geo

import kotlin.math.*

class Box(val lng1: Double, val lat1: Double, val lng2: Double, val lat2: Double) {

    val heightInKilometers: Double
        get() {
            return haversine(lat1 = lat1, lon1 = lng1, lat2 = lat2, lon2 = lng1)
        }

    val widthInKilometers: Double
        get() {
            return haversine(lat1 = lat1, lon1 = lng1, lat2 = lat1, lon2 = lng2)
        }

    fun inside(oBox: Box): Boolean {
        if (oBox.lng1 >= lng1 && oBox.lng2 <= lng2 && oBox.lat1 >= lat1 && oBox.lat2 <= lat2) {
            return true
        }
        return false
    }

    fun outside(oBox: Box): Boolean {
        if (lng2 < oBox.lng1 || lng1 > oBox.lng2) return true
        if (lat2 < oBox.lat1 || lat1 > oBox.lat2) return true
        return false
    }


    private fun haversine(lat1: Double, lon1: Double,
                          lat2: Double, lon2: Double): Double {

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) *
                cos(rLat1) *
                cos(rLat2)
        val rad = 6371.0
        val c = 2 * asin(sqrt(a))
        return rad * c
    }

    override fun toString(): String {
        return "[[$lat1, $lng1], [$lat2, $lng2]]"
    }
}
