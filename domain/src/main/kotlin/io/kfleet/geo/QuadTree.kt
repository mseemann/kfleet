package io.kfleet.geo

class QuadTree {

    companion object {

        fun encodedIndexPath(lng: Double, lat: Double): String {
            val quadrants = indexPath(lng = lng, lat = lat)
            return quadrants.joinToString("/") { it.quadrant.label }
        }

        fun indexPath(lng: Double, lat: Double): List<Node> {
            val quadrants = mutableListOf<Node>()
            var currentNode = rootNode
            for (i in 1..INDEX_PATH_LENGTH) {
                val q = currentNode.quadrantForPosition(lng = lng, lat = lat)
                quadrants.add(q)
                currentNode = q
            }
            return quadrants
        }

        fun getIntersectingIndexes(lng: Double, lat: Double, withDistanceInKilometers: Double): TreeNode {
            val box = GeoTools.surroundingBox(lng = lng, lat = lat, withDistanceInKilometers = withDistanceInKilometers)

            return rootNode.buildIntersectionPaths(box)
        }


        fun boundingBoxes(nodes: List<Node>): List<Box> {
            return nodes.map { it.boundingBox() }
        }
    }

}
