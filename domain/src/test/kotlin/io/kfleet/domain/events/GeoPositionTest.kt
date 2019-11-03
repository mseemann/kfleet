package io.kfleet.domain.events

import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec

class GeoPositionTest : BehaviorSpec({

    Given("GeoPosition for cars") {

        When("a position is created") {
            val pos = geoPositionCarLocation {
                lng = 1.0
                lat = 1.0
            }
            Then("a path index must be available") {
                pos.toQuadrantIndex().shouldBe("2/4/4/4/4/4/4/1/3/1/2/3")
            }
        }
    }
})
