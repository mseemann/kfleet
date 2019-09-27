package io.kfleet.cars.service.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals


class JsonTest {

    @Test
    fun testSerializeACar() {
        val mapper = jacksonObjectMapper()


        val car = Car("1", Random.nextDouble(0.0, 100.0), CarState.FREE, GeoPositionCreator.create())

        val serialized = mapper.writeValueAsString(car)

        val car2: Car = mapper.readValue(serialized)

        assertEquals(car, car2)
    }


}
