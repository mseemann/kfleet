package io.kfleet.domain.events

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroRuntimeException

class RideEventsTest : BehaviorSpec({

    Given("RideRequested events") {

        When("all properties are provide") {
            val event = rideRequestedEvent {
                travelerId = "1"
                requestTime = "T"
                from = geoPositionRideRequested {
                    lat = 1.0
                    lng = 1.0
                }
                fromGeoIndex = "1/1/2"
                to = geoPositionRideRequested {
                    lat = 1.0
                    lng = 1.0
                }
            }
            Then("an event should be created") {
                event.shouldNotBeNull()
            }

            Then("fromGeoIndex should be the message key") {
                event.asKeyValue().key.shouldBe(event.getFromGeoIndex())
            }

            Then("the 'isRideEvent' method should return true") {
                event.isRideEvent().shouldBeTrue()
            }
        }

        When("no properties are provided") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    rideRequestedEvent {}
                }
            }
        }
    }
})
