package io.kfleet.domain.events

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroRuntimeException

class CarEventsTest : BehaviorSpec({

    Given("owner car registered events") {
        When("a carId is specified") {
            val carRegisteredEvent = carRegisteredEvent {
                carId = "1"
            }

            Then("an event should be created") {
                carRegisteredEvent.shouldNotBeNull()
            }

            Then("carid should be the message key") {
                carRegisteredEvent.asKeyValue().key.shouldBe(carRegisteredEvent.getCarId())
            }

            Then("the 'isCarEvent' method should return true") {
                carRegisteredEvent.isCarEvent().shouldBeTrue()
            }
        }

        When("no car id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    carRegisteredEvent {}
                }
            }
        }

    }

    Given("owner car deregistered events") {
        When("a carId is specified") {
            val carDeregisteredEvent = carDeregisteredEvent {
                carId = "1"
            }

            Then("an event should be created") {
                carDeregisteredEvent.shouldNotBeNull()
            }

            Then("carid should be the message key") {
                carDeregisteredEvent.asKeyValue().key.shouldBe(carDeregisteredEvent.getCarId())
            }

            Then("the 'isCarEvent' method should return true") {
                carDeregisteredEvent.isCarEvent().shouldBeTrue()
            }
        }

        When("no car id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    carDeregisteredEvent {}
                }
            }
        }
    }

    Given("car locaiton changed events") {
        When("a carId and positon is specified") {
            val carLocationChangedEvent = carLocationChangedEvent {
                carId = "1"
                geoPosition = GeoPositionFactory.createRandomCarLocation()
                geoPositionIndex = "1/1/2..."
            }

            Then("an event should be created") {
                carLocationChangedEvent.shouldNotBeNull()
            }

            Then("carid should be the message key") {
                carLocationChangedEvent.asKeyValue().key.shouldBe(carLocationChangedEvent.getCarId())
            }

            Then("the 'isCarLocationEvent' method should return true") {
                carLocationChangedEvent.isCarLocationEvent().shouldBeTrue()
            }
        }

        When("no car id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    carLocationChangedEvent {}
                }
            }
        }
    }
})
