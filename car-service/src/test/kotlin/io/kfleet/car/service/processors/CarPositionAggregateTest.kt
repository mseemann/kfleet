package io.kfleet.car.service.processors

import io.kfleet.car.service.processor.CarPositionAggregate
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.expect

class CarPositionAggregateTest {

    @Test
    fun testAddSubAggregate() {
        val agg = CarPositionAggregate()
        agg.add("1", "c1")
        assertTrue { agg.quadrants["1"]!!.size == 1 }
        assertTrue { agg.quadrants["1"]!!.contains("c1") }

        agg.add("1", "c2")
        assertTrue { agg.quadrants["1"]!!.size == 2 }
        assertTrue { agg.quadrants["1"]!!.contains("c1") }
        assertTrue { agg.quadrants["1"]!!.contains("c2") }

        agg.sub("1", "c1")
        expect("CarPositionAggregate(quadrants={1=[c2]})") { agg.toString() }
    }

}
