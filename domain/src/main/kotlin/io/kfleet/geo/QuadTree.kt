package io.kfleet.geo

class QuadTree {

    companion object {

        fun encodedIndexPath(lng: Double, lat: Double): String {
            val quadrants = indexPath(lng = lng, lat = lat)
            return quadrants.map { it.quadrant.label }.joinToString("/")
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

        fun getIntersectingIndexes(lng: Double, lat: Double, withDistanceInKilometers: Double): List<String> {
            val box = GeoTools.surroundingBox(lng = lng, lat = lat, withDistanceInKilometers = withDistanceInKilometers)

            val treeRoot = rootNode.buildIntersectionPaths(box)

            treeRoot.printTree()

            return treeRoot.getIndexPaths()
        }


        fun boundingBoxes(nodes: List<Node>): List<Box> {
            return nodes.map { it.boundingBox() }
        }
    }

}
