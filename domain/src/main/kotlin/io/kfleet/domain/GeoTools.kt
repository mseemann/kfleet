package io.kfleet.domain

// https://wiki.openstreetmap.org/wiki/Precision_of_coordinates


enum class Quadrant(val label: String) {
    NW("1"), NE("2"), SE("3"), SW("4")
}

private data class QuadrantAndNode(val quadrant: Quadrant, val node: Node)

private class Node(val x: Double, val y: Double, val w: Double, val h: Double) {

    private val w2 = w / 2
    private val h2 = h / 2
    private val midX = x + w2
    private val midY = y + h2

    fun quadrant(lat: Double, lng: Double): QuadrantAndNode {
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

}


class QuadTree {

    companion object {
        const val INDEX_PATH_LENGTH = 11
        // change the coordinate system, so that we do not need to deal with negative values
        const val NORMALIZE_LNG = 180
        const val NORMALIZE_LAT = 90

        private val rootNode = Node(x = -180.0 + NORMALIZE_LNG, y = -90.0 + NORMALIZE_LAT, w = 360.0, h = 180.0)

        fun index(lng: Double, lat: Double): String {
            val normX = lng + NORMALIZE_LNG
            val normY = lat + NORMALIZE_LAT
            var currentNode = rootNode;
            val quadrants = mutableListOf<Quadrant>()
            for (i in 1..INDEX_PATH_LENGTH) {
                val q = currentNode.quadrant(lng = normX, lat = normY)
                quadrants.add(q.quadrant)
                currentNode = q.node
            }
            return quadrants.map { it.label }.joinToString("/")
        }
    }

}
