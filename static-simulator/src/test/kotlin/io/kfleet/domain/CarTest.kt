package io.kfleet.domain

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertEquals

@RunWith(SpringRunner::class)
@SpringBootTest()
class CarTest {

    @Test
    fun testCanReach() {
        val car = Car(
            id = "1",
            stateOfCharge = 50.0,
            geoPosition = GeoPosition.random()
        );
        assertEquals(car.canReach(), 220.0)
    }
}
