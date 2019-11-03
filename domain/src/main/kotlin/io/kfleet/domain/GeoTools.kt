package io.kfleet.domain

// x = lng, y = lat
// origin: lower left
// numbering of the quadrants is clockwise - starting north west
// thre is no projection that creates an equal area - so every quadrant
// has different sizes - but this is ok for this poc.

enum class Quadrant(val label: String) {
    NW("1"), NE("2"), SE("3"), SW("4")
}

data class QuadrantAndNode(val quadrant: Quadrant, val node: Node)

class Node(val x: Double, val y: Double, val w: Double, val h: Double) {

    private val w2 = w / 2
    private val h2 = h / 2
    private val midX = x + w2
    private val midY = y + h2

    fun quadrantForPosition(lat: Double, lng: Double): QuadrantAndNode {
        return if (lng < midX) {
            if (lat < midY)
                QuadrantAndNode(Quadrant.SW, Node(x, y, w2, h2))
            else QuadrantAndNode(Quadrant.NW, Node(x, midY, w2, h2))
        } else {
            if (lat < midY)
                QuadrantAndNode(Quadrant.SE, Node(midX, y, w2, h2))
            else QuadrantAndNode(Quadrant.NE, Node(midX, midY, w2, h2))
        }
    }

    fun boundingBox() = Box(lng1 = x, lat1 = y, lng2 = x + w, lat2 = y + h)

    override fun toString(): String {
        return "Node(x=$x, y=$y, w=$w, h=$h)"
    }

}

class Box(val lng1: Double, val lat1: Double, val lng2: Double, val lat2: Double) {

    val heightInKilometers: Double
        get() {
            return haversine(lat1 = lat1, lon1 = lng1, lat2 = lat2, lon2 = lng1)
        }

    val widthInKilometers: Double
        get() {
            return haversine(lat1 = lat1, lon1 = lng1, lat2 = lat1, lon2 = lng2)
        }


    private fun haversine(lat1: Double, lon1: Double,
                          lat2: Double, lon2: Double): Double {
        var lat1 = lat1
        var lat2 = lat2
        // distance between latitudes and longitudes
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        // convert to radians
        lat1 = Math.toRadians(lat1)
        lat2 = Math.toRadians(lat2)

        // apply formulae
        val a = Math.pow(Math.sin(dLat / 2), 2.0) + Math.pow(Math.sin(dLon / 2), 2.0) *
                Math.cos(lat1) *
                Math.cos(lat2)
        val rad = 6371.0
        val c = 2 * Math.asin(Math.sqrt(a))
        return rad * c
    }

    override fun toString(): String {
        return "[[$lat1, $lng1], [$lat2, $lng2]]"
    }
}


class QuadTree {

    companion object {
        const val INDEX_PATH_LENGTH = 11

        private val rootNode = Node(x = -180.0, y = -90.0, w = 360.0, h = 180.0)

        fun encodedIndexPath(lng: Double, lat: Double): String {
            val quadrants = indexPath(lng = lng, lat = lat)
            return quadrants.map { it.quadrant.label }.joinToString("/")
        }

        fun indexPath(lng: Double, lat: Double): List<QuadrantAndNode> {
            val quadrants = mutableListOf<QuadrantAndNode>()
            var currentNode = rootNode;
            for (i in 1..INDEX_PATH_LENGTH) {
                val q = currentNode.quadrantForPosition(lng = lng, lat = lat)
                quadrants.add(q)
                currentNode = q.node
            }
            return quadrants
        }

        fun getIntersectingIndexes(lng: Double, lat: Double, withDistanceInKilometers: Double): List<String> {
            // create a bounding box for the parameters (x=lng, y=lat, w, h)
            val box = GeoTools.surroundingBox(lng = lng, lat = lat, withDistanceInKilometers = withDistanceInKilometers)

            // find all quads at level INDEX_PATH_LENGTH that intersect with this bounding box

            return listOf()
        }


        fun boundingBoxes(quadrantAndNodes: List<QuadrantAndNode>): List<Box> {
            return quadrantAndNodes.map { it.node.boundingBox() }
        }
    }

}

class GeoTools {

    companion object {

        fun surroundingBox(lng: Double, lat: Double, withDistanceInKilometers: Double): Box {
            val rLat = Math.toRadians(lat)
            val degreesInLatDirection = withDistanceInKilometers / 111.111
            val degreesInLngDirection = withDistanceInKilometers / Math.cos(rLat) / 111.111

            val x = lng - degreesInLngDirection / 2
            val y = lat - degreesInLatDirection / 2
            val w = degreesInLngDirection
            val h = degreesInLatDirection

            return Box(lng1 = x, lat1 = y, lng2 = x + w, lat2 = y + h)
        }
    }
}

