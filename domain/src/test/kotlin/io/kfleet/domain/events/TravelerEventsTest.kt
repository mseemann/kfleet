package io.kfleet.domain.events

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroMissingFieldException

class TravelerEventsTest : BehaviorSpec({

    Given("traveler created events") {

        When("an travelerId, name and email is specified") {
            val travelerCreatedEvent = travelerCreatedEvent {
                travelerId = "1"
                name = "nay name"
                email = "a@a.com"
            }

            Then("an event should be created") {
                travelerCreatedEvent.shouldNotBeNull()
            }

            Then("travelerId should be the message key") {
                travelerCreatedEvent.should {
                    it.asKeyValue().key === travelerCreatedEvent.getTravelerId()
                }
            }

            Then("the 'isTravelerEvent' method should return true") {
                travelerCreatedEvent.isTravelerEvent().shouldBeTrue()
            }
        }

        When("only the traveler id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    travelerCreatedEvent {
                        travelerId = "1"
                    }
                }
            }
        }
    }

    Given("traveler deleted events") {
        When("a travelerID is specified") {
            val travelerDeletedEvent = travelerDeletedEvent {
                travelerId = "1"
            }

            Then("an event should be created") {
                travelerDeletedEvent.shouldNotBeNull()
            }

            Then("traveler should be the message key") {
                travelerDeletedEvent.should {
                    it.asKeyValue().key === travelerDeletedEvent.getTravelerId()
                }
            }

            Then("the 'isTravelerEvent' method should return true") {
                travelerDeletedEvent.isTravelerEvent().shouldBeTrue()
            }
        }

        When(" no traveler id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    travelerDeletedEvent {}
                }
            }
        }
    }
})
