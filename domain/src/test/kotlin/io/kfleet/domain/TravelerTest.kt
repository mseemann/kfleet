package io.kfleet.domain

import org.junit.jupiter.api.Test
import kotlin.test.expect

class TravelerTest {

    @Test
    fun testCreateTraveler() {
        val traveler = Traveler.create(1)
        expect(TravelerStatus.IS_LIVING) { traveler.state }
    }
}

