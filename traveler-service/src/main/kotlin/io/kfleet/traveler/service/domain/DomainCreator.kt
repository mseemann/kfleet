package io.kfleet.traveler.service.domain

import io.kfleet.domain.events.ride.GeoPositionRideRequestedEvent
import io.kfleet.geo.QuadTree
import io.kfleet.traveler.service.commands.GeoPositionCarRequest


fun geoPositionCarRequest(buildGeoPosition: GeoPositionCarRequest.Builder.() -> Unit): GeoPositionCarRequest =
        GeoPositionCarRequest.newBuilder().apply { buildGeoPosition() }.build()

fun GeoPositionCarRequest.toQuadrantIndex(): String {
    return QuadTree.encodedIndexPath(lng = this.getLng(), lat = this.getLat())
}

fun geoPositionRideRequest(buildGeoPosition: GeoPositionRideRequestedEvent.Builder.() -> Unit): GeoPositionRideRequestedEvent =
        GeoPositionRideRequestedEvent.newBuilder().apply { buildGeoPosition() }.build()

fun GeoPositionCarRequest.toGeoPositionRideRequest(): GeoPositionRideRequestedEvent {
    val lat = getLat()
    val lng = getLng()
    return geoPositionRideRequest {
        setLat(lat)
        setLng(lng)
    }

}
