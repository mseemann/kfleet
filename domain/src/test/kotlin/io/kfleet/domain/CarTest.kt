package io.kfleet.domain

import org.junit.Test

import kotlin.test.assertEquals

class CarTest {

    @Test
    fun testCanReach() {
        val car = Car(
                id = "1",
                stateOfCharge = 50.0,
                geoPosition = GeoPosition.random()
        )
        assertEquals(car.canReach(), 220.0)
    }
}
