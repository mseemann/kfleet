package io.kfleet.traveler.service.domain

import io.kfleet.geo.QuadTree
import io.kfleet.traveler.service.commands.GeoPositionCarRequest


fun geoPositionCarRequest(buildGeoPosition: GeoPositionCarRequest.Builder.() -> Unit): GeoPositionCarRequest =
        GeoPositionCarRequest.newBuilder().apply { buildGeoPosition() }.build()

fun GeoPositionCarRequest.toQuadrantIndex(): String {
    return QuadTree.encodedIndexPath(lng = this.getLng(), lat = this.getLat())
}
