package io.kfleet.domain

// x = lng, y = lat
// origin: lower left
// numbering of the quadrants is clockwise - starting north west
// thre is no projection that creates an equal area - so every quadrant
// has different sizes - but this is ok for this poc.

enum class Quadrant(val label: String) {
    R("0"), NW("1"), NE("2"), SE("3"), SW("4")
}

class Node(val x: Double, val y: Double, val w: Double, val h: Double, val quadrant: Quadrant) {

    private val w2 = w / 2
    private val h2 = h / 2
    private val midX = x + w2
    private val midY = y + h2

    private val nw = lazy { Node(x, midY, w2, h2, Quadrant.NW) }
    private val ne = lazy { Node(midX, midY, w2, h2, Quadrant.NE) }
    private val se = lazy { Node(midX, y, w2, h2, Quadrant.SE) }
    private val sw = lazy { Node(x, y, w2, h2, Quadrant.SW) }

    private val childs = mutableListOf<Node>()

    fun quadrantForPosition(lat: Double, lng: Double): Node {
        return if (lng < midX) {
            if (lat < midY) sw.value else nw.value
        } else {
            if (lat < midY) se.value else ne.value
        }
    }

    fun boundingBox() = Box(lng1 = x, lat1 = y, lng2 = x + w, lat2 = y + h)

    private fun intersectsWith(box: Box): Boolean {
        if (box.inside(boundingBox()) || boundingBox().inside(box)) {
            return true
        }
        if (box.outside(boundingBox()) || boundingBox().outside(box)) {
            return false
        }
        return true;
    }

    fun intersectingQuadrants(box: Box, level: Int, maxLevel: Int): List<Node> {
        arrayOf(nw, ne, se, sw).forEach {
            if (it.value.intersectsWith(box)) {
                childs.add(it.value)
                if (level + 1 <= maxLevel)
                    it.value.intersectingQuadrants(box, level + 1, maxLevel)
            }
        }
        return childs
    }

    override fun toString(): String {
        return "Node(x=$x, y=$y, w=$w, h=$h, quadrant=${quadrant.name})"
    }

    private fun printTree(level: Int) {
        println(" ".repeat(level) + childs.map { it.quadrant.label })
        childs.forEach { it.printTree(level + 1) }
    }

    fun printTree() {
        this.printTree(1)
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
        private const val INDEX_PATH_LENGTH = 12

        private val rootNode = Node(x = -180.0, y = -90.0, w = 360.0, h = 180.0, quadrant = Quadrant.R)

        fun encodedIndexPath(lng: Double, lat: Double): String {
            val quadrants = indexPath(lng = lng, lat = lat)
            return quadrants.map { it.quadrant.label }.joinToString("/")
        }

        fun indexPath(lng: Double, lat: Double): List<Node> {
            val quadrants = mutableListOf<Node>()
            var currentNode = rootNode;
            for (i in 1..INDEX_PATH_LENGTH) {
                val q = currentNode.quadrantForPosition(lng = lng, lat = lat)
                quadrants.add(q)
                currentNode = q
            }
            return quadrants
        }

        fun getIntersectingIndexes(lng: Double, lat: Double, withDistanceInKilometers: Double): List<String> {
            // create a bounding box for the parameters (x=lng, y=lat, w, h)
            val box = GeoTools.surroundingBox(lng = lng, lat = lat, withDistanceInKilometers = withDistanceInKilometers)

            // find all quads up to level INDEX_PATH_LENGTH that intersect with this bounding box
            val intersectingQuadrants = rootNode.intersectingQuadrants(box, 1, INDEX_PATH_LENGTH)

            rootNode.printTree()



            return listOf()
        }


        fun boundingBoxes(ndoes: List<Node>): List<Box> {
            return ndoes.map { it.boundingBox() }
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

