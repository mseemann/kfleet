package io.kfleet.traveler.service.domain


fun geoPosition(buildGeoPosition: GeoPosition.Builder.() -> Unit): GeoPosition =
        GeoPosition.newBuilder().apply { buildGeoPosition() }.build()
