package io.kfleet.traveler.service.domain

import io.kfleet.traveler.service.commands.GeoPositionCarRequest


fun geoPositionCarRequest(buildGeoPosition: GeoPositionCarRequest.Builder.() -> Unit): GeoPositionCarRequest =
        GeoPositionCarRequest.newBuilder().apply { buildGeoPosition() }.build()
