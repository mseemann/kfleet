package io.kfleet.geo

class Node(val x: Double, val y: Double, val w: Double, val h: Double, val quadrant: Quadrant) {

    private val w2 = w / 2
    private val h2 = h / 2
    private val midX = x + w2
    private val midY = y + h2

    private val nw = lazy { Node(x, midY, w2, h2, Quadrant.NW) }
    private val ne = lazy { Node(midX, midY, w2, h2, Quadrant.NE) }
    private val se = lazy { Node(midX, y, w2, h2, Quadrant.SE) }
    private val sw = lazy { Node(x, y, w2, h2, Quadrant.SW) }

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
        return true
    }

    private fun buildIntersectionPaths(parent: TreeNode, box: Box, level: Int, maxLevel: Int) {
        arrayOf(nw, ne, se, sw).forEach {
            if (it.value.intersectsWith(box)) {
                val child = TreeNode(it.value)
                parent.childs.add(child)
                if (level + 1 <= maxLevel)
                    it.value.buildIntersectionPaths(child, box, level + 1, maxLevel)
            }
        }
    }

    fun buildIntersectionPaths(box: Box): TreeNode {
        val treeRoot = TreeNode(rootNode)
        buildIntersectionPaths(treeRoot, box, 1, INDEX_PATH_LENGTH)
        return treeRoot
    }

    override fun toString(): String {
        return "Node(x=$x, y=$y, w=$w, h=$h, quadrant=${quadrant.name})"
    }

}


class TreeNode(val node: Node) {

    val childs = mutableListOf<TreeNode>()

    private fun auxPrintTree(level: Int) {
        if (childs.isEmpty()) {
            return
        }
        println(" ".repeat(level) + "$level " + childs.map { it.node.quadrant.label })
        childs.forEach { it.auxPrintTree(level + 1) }
    }

    fun printTree() = auxPrintTree(1)

    private fun auxGetIndexPaths(): List<List<String>> {
        if (childs.isEmpty()) {
            return listOf(listOf(node.quadrant.label))
        }

        var l = listOf<List<String>>()
        childs.forEach {
            val x = it.auxGetIndexPaths().map { list ->
                if (node.quadrant != Quadrant.R)
                    list.plus(listOf(node.quadrant.label))
                else list
            }
            l = l.plus(x)
        }
        return l
    }

    fun getIndexPaths(): List<String> = auxGetIndexPaths().map {
        // reverse - because the nodes are collected from the leafs upwards
        it.reversed().joinToString("/")
    }
}

