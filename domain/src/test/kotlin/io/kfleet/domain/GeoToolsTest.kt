package io.kfleet.domain

import io.kfleet.domain.events.OsloLatRange
import io.kfleet.domain.events.OsloLngRange
import org.junit.jupiter.api.Test
import kotlin.test.expect

class GeoToolsTest {

    @Test
    fun testOsloNorthWest() {
        val lat = OsloLatRange.get(1)
        val lng = OsloLngRange.get(0)

        val quadrantAndNodes = QuadTree.indexPath(lng = lng, lat = lat)
        expect(11) { quadrantAndNodes.size }

        val boxes = QuadTree.boundingBoxes(quadrantAndNodes);
        boxes.forEach { println(it) }

        val firstNode = quadrantAndNodes.get(0).node
        expect(Node(0.0, 0.0, w = 180.0, h = 90.0).toString()) { firstNode.toString() }

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/4/2/2") { index }
    }

    @Test
    fun testOsloNorthEast() {
        val lat = OsloLatRange.get(1)
        val lng = OsloLngRange.get(1)

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/3/2/1") { index }
    }

    @Test
    fun testOsloSouthEast() {
        val lat = OsloLatRange.get(0)
        val lng = OsloLngRange.get(1)

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/3/3/4") { index }
    }

    @Test
    fun testOsloSouthWest() {
        val lat = OsloLatRange.get(0)
        val lng = OsloLngRange.get(0)

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/4/3/3") { index }
    }

    @Test
    fun testSaoPaulo() {
        val lng = -46.616667
        val lat = -23.5
        val quadrantAndNodes = QuadTree.indexPath(lng = lng, lat = lat)
        expect(11) { quadrantAndNodes.size }

        val boxes = QuadTree.boundingBoxes(quadrantAndNodes);
        boxes.forEach { println(it) }

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("4/2/4/2/2/2/2/4/2/3/4") { index }
    }

    @Test
    fun testIndexPathsForBoundingBoxRoot() {
        val paths = QuadTree.getIndexesArround(lng = 0.0, lat = 0.0, withMinDistanceInKilometers = 0.0)
        expect(0) { paths.size }
    }
}
