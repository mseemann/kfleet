package io.kfleet.cars.service.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.GeoPosition
import org.junit.Test
import kotlin.test.assertEquals


class JsonTest {

    @Test
    fun tserializeCar() {
        val mapper = jacksonObjectMapper()
        val car = Car(id = "1", state = CarStatus.FREE, geoPosition = GeoPosition.random(), stateOfCharge = 10.0)

        val serialized = mapper.writeValueAsString(car)

        val car2: Car = mapper.readValue(serialized)

        assertEquals(car, car2)
    }


}
