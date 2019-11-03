package io.kfleet.geo

// x = lng, y = lat
// origin: lower left
// numbering of the quadrants is clockwise - starting north west
// there is no projection that creates an equal area - so every quadrant
// has different sizes - but this is ok for this poc.

enum class Quadrant(val label: String) {
    R("0"), NW("1"), NE("2"), SE("3"), SW("4")
}

const val INDEX_PATH_LENGTH = 12

val rootNode = Node(x = -180.0, y = -90.0, w = 360.0, h = 180.0, quadrant = Quadrant.R)


class GeoTools {

    companion object {

        const val KILOMETER_PER_DEGREE = 111.111

        fun surroundingBox(lng: Double, lat: Double, withDistanceInKilometers: Double): Box {
            val rLat = Math.toRadians(lat)
            val degreesInLatDirection = withDistanceInKilometers / KILOMETER_PER_DEGREE
            val degreesInLngDirection = withDistanceInKilometers / Math.cos(rLat) / KILOMETER_PER_DEGREE

            val x = lng - degreesInLngDirection / 2
            val y = lat - degreesInLatDirection / 2
            val w = degreesInLngDirection
            val h = degreesInLatDirection

            return Box(lng1 = x, lat1 = y, lng2 = x + w, lat2 = y + h)
        }
    }
}

