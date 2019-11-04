package io.kfleet.car.service.domain

import io.kfleet.domain.events.GeoPositionFactory
import io.kfleet.domain.events.car.GeoPositionCarLocation
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import kotlin.random.Random

fun car(buildCar: Car.Builder.() -> Unit): Car = Car.newBuilder().apply { buildCar() }.build()

fun Car.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getId(), this)
}

fun GeoPositionCarLocation.toCarLocation(): GeoPositionCar {
    val lat = getLat()
    val lng = getLng()
    return geoPositionCar {
        setLat(lat)
        setLng(lng)
    }
}


object CarFactory {

    fun createRandom(id: Int) = Car(
            "$id",
            Random.nextDouble(0.0, 100.0),
            CarState.values()[Random.nextInt(CarState.values().size)],
            GeoPositionFactory.createRandomCarLocation().toCarLocation()
    )
}


fun geoPositionCar(buildGeoPosition: GeoPositionCar.Builder.() -> Unit): GeoPositionCar =
        GeoPositionCar.newBuilder().apply { buildGeoPosition() }.build()
