package io.kfleet.domain.events

import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroMissingFieldException

class OwnerEventsTests : BehaviorSpec({
    Given("owner create events") {

        When("an ownerId and name is specified") {
            val ownerCreatedEvent = ownerCreated {
                ownerId = "1"
                name = "nay name"
            }

            Then("an event should be created") {
                ownerCreatedEvent.shouldNotBeNull()
            }
        }
        
        When("only the owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    ownerCreated {
                        ownerId = "1"
                    }
                }
            }
        }
    }
})
