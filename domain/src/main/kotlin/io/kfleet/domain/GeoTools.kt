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

    private val nw = lazy { QuadrantAndNode(Quadrant.NW, Node(x, midY, w2, h2)) }
    private val ne = lazy { QuadrantAndNode(Quadrant.NE, Node(midX, midY, w2, h2)) }
    private val se = lazy { QuadrantAndNode(Quadrant.SE, Node(midX, y, w2, h2)) }
    private val sw = lazy { QuadrantAndNode(Quadrant.SW, Node(x, y, w2, h2)) }

    fun quadrantForPosition(lat: Double, lng: Double): QuadrantAndNode {
        return if (lng < midX) {
            if (lat < midY) sw.value else nw.value
        } else {
            if (lat < midY) se.value else ne.value
        }
    }

    fun boundingBox() = Box(lng1 = x, lat1 = y, lng2 = x + w, lat2 = y + h)

    fun intersectsWith(box: Box): Boolean {
        println(box)
        println(boundingBox())
        if (box.inside(boundingBox()) || boundingBox().inside(box)) {
            println("inside = true")
            return true
        }
        if (box.outside(boundingBox()) || boundingBox().outside(box)) {
            println("ouside = false")
            return false
        }
        println("not inside not outside = true")
        return true;
    }

    fun intersectingQuadrants(box: Box): List<QuadrantAndNode> {
        val result = mutableListOf<QuadrantAndNode>()
        if (nw.value.node.intersectsWith(box)) {
            result.add(nw.value)
        }
        if (ne.value.node.intersectsWith(box)) {
            result.add(ne.value)
        }
        if (se.value.node.intersectsWith(box)) {
            result.add(se.value)
        }
        if (sw.value.node.intersectsWith(box)) {
            result.add(sw.value)
        }
        return result
    }

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

    fun inside(oBox: Box): Boolean {
        if (oBox.lng1 >= lng1 && oBox.lng2 <= lng2 && oBox.lat1 >= lat1 && oBox.lat2 <= lat2) {
            return true;
        }
        return false;
    }

    fun outside(oBox: Box): Boolean {
        if (lng2 < oBox.lng1 || lng1 > oBox.lng2) return true;
        if (lat2 < oBox.lat1 || lat1 > oBox.lat2) return true;
        return false;
    }


    private fun haversine(lat1: Double, lon1: Double,
                          lat2: Double, lon2: Double): Double {

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val rLat1 = Math.toRadians(lat1)
        val rLat2 = Math.toRadians(lat2)

        val a = Math.pow(Math.sin(dLat / 2), 2.0) + Math.pow(Math.sin(dLon / 2), 2.0) *
                Math.cos(rLat1) *
                Math.cos(rLat2)
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

            // find all quads up to level INDEX_PATH_LENGTH that intersect with this bounding box
            val intersectingQuadrants = rootNode.intersectingQuadrants(box)
            println(intersectingQuadrants)
            intersectingQuadrants.forEach {
                val x = it.node.intersectingQuadrants(box)
                println(x)
            }


            return listOf()
        }


        fun boundingBoxes(quadrantAndNodes: List<QuadrantAndNode>): List<Box> {
            return quadrantAndNodes.map { it.node.boundingBox() }
        }
    }

}

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

