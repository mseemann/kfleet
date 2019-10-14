package io.kfleet.owner.service.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.car.service.domain.GeoPositionFactory
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import io.kfleet.common.configuration.MixInIgnoreAvroSchemaProperties
import org.apache.avro.specific.SpecificRecord
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals


class JsonTest {

    @Test
    fun testSerializeACar() {
        val mapper = jacksonObjectMapper()
        mapper.addMixIn(SpecificRecord::class.java, MixInIgnoreAvroSchemaProperties::class.java)


        val car = Car("1", Random.nextDouble(0.0, 100.0), CarState.FREE, GeoPositionFactory.createRandom())

        val serialized = mapper.writeValueAsString(car)

        val car2: Car = mapper.readValue(serialized)

        assertEquals(car, car2)
    }


}
