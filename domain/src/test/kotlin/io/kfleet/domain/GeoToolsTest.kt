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

        val nodes = QuadTree.indexPath(lng = lng, lat = lat)
        expect(12) { nodes.size }

        val boxes = QuadTree.boundingBoxes(nodes);
        boxes.forEach { println(it) }

        val firstNode = nodes.get(0)
        expect(Node(0.0, 0.0, w = 180.0, h = 90.0, quadrant = Quadrant.NE).toString()) { firstNode.toString() }

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/4/2/2/4") { index }
    }

    @Test
    fun testOsloNorthEast() {
        val lat = OsloLatRange.get(1)
        val lng = OsloLngRange.get(1)

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/3/2/1/4") { index }
    }

    @Test
    fun testOsloSouthEast() {
        val lat = OsloLatRange.get(0)
        val lng = OsloLngRange.get(1)

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/3/3/4/4") { index }
    }

    @Test
    fun testOsloSouthWest() {
        val lat = OsloLatRange.get(0)
        val lng = OsloLngRange.get(0)

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("2/1/4/1/4/2/3/2/4/3/3/4") { index }
    }

    @Test
    fun testSaoPaulo() {
        val lng = -46.616667
        val lat = -23.5
        val quadrantAndNodes = QuadTree.indexPath(lng = lng, lat = lat)
        expect(12) { quadrantAndNodes.size }

        val boxes = QuadTree.boundingBoxes(quadrantAndNodes);
        boxes.forEach { println(it) }

        val index = QuadTree.encodedIndexPath(lng = lng, lat = lat)
        expect("4/2/4/2/2/2/2/4/2/3/4/2") { index }
    }

    @Test
    fun testBoundingBoxes() {
        val distance: Long = 10
        val boxOslo = GeoTools.surroundingBox(lat = 59.9134, lng = 10.6788, withDistanceInKilometers = distance.toDouble())
        expect(distance) { Math.round(boxOslo.heightInKilometers) }
        expect(distance) { Math.round(boxOslo.widthInKilometers) }

        val boxSaoPaulo = GeoTools.surroundingBox(lat = -23.5, lng = -46.616667, withDistanceInKilometers = distance.toDouble())
        expect(distance) { Math.round(boxSaoPaulo.heightInKilometers) }
        expect(distance) { Math.round(boxSaoPaulo.widthInKilometers) }

        val boxEquator = GeoTools.surroundingBox(lat = 0.0, lng = 0.0, withDistanceInKilometers = distance.toDouble())
        expect(distance) { Math.round(boxEquator.heightInKilometers) }
        expect(distance) { Math.round(boxEquator.widthInKilometers) }
    }

    @Test
    fun testIndexPathsForBoundingBoxRoot() {
        val paths = QuadTree.getIntersectingIndexes(lng = 10.6088, lat = 59.9134, withDistanceInKilometers = 5.0)
        expect(0) { paths.size }
    }
}
