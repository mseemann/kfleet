package io.kfleet.car.service.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.common.configuration.MixInIgnoreAvroSchemaProperties
import io.kfleet.domain.events.GeoPositionFactory
import org.apache.avro.specific.SpecificRecord
import org.junit.jupiter.api.Test
import kotlin.random.Random
import kotlin.test.assertEquals


class JsonTest {

    @Test
    fun testSerializeACar() {
        val mapper = jacksonObjectMapper()
        mapper.addMixIn(SpecificRecord::class.java, MixInIgnoreAvroSchemaProperties::class.java)


        val car = Car("1", Random.nextDouble(0.0, 100.0), CarState.FREE, GeoPositionFactory.createRandom().toCarLocation())

        val serialized = mapper.writeValueAsString(car)

        val car2: Car = mapper.readValue(serialized)

        assertEquals(car, car2)
    }


}
