package io.kfleet.domain.events


import io.kfleet.domain.QuadTree
import io.kfleet.domain.events.car.GeoPositionCarLocation
import kotlin.random.Random

val OsloLatRange = arrayOf(59.7984951859, 60.0334203198)
val OsloLngRange = arrayOf(10.3772388202, 10.9805373651)

fun geoPositionCarLocation(buildGeoPosition: GeoPositionCarLocation.Builder.() -> Unit): GeoPositionCarLocation =
        GeoPositionCarLocation.newBuilder().apply { buildGeoPosition() }.build()

object GeoPositionFactory {

    fun createRandom() = geoPositionCarLocation {
        lat = Random.nextDouble(OsloLatRange[0], OsloLatRange[1])
        lng = Random.nextDouble(OsloLngRange[0], OsloLngRange[1])
    }
}

fun GeoPositionCarLocation.toQuadrantIndex(): String {
    return QuadTree.encodedIndexPath(lng = this.getLng(), lat = this.getLat())
}

