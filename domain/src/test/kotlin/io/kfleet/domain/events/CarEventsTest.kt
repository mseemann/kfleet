package io.kfleet.domain.events

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroMissingFieldException

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
                carRegisteredEvent.should {
                    it.asKeyValue().key === carRegisteredEvent.getCarId()
                }
            }

            Then("the 'isCarEvent' method should return true") {
                carRegisteredEvent.isCarEvent().shouldBeTrue()
            }
        }

        When("no car id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
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
                carDeregisteredEvent.should {
                    it.asKeyValue().key === carDeregisteredEvent.getCarId()
                }
            }

            Then("the 'isCarEvent' method should return true") {
                carDeregisteredEvent.isCarEvent().shouldBeTrue()
            }
        }

        When("no car id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    carDeregisteredEvent {}
                }
            }
        }
    }
})
