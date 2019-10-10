package io.kfleet.domain.events

import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroMissingFieldException

class OwnerEventsTest : BehaviorSpec({

    Given("owner created events") {

        When("an ownerId and name is specified") {
            val ownerCreatedEvent = ownerCreated {
                ownerId = "1"
                name = "nay name"
            }

            Then("an event should be created") {
                ownerCreatedEvent.shouldNotBeNull()
            }

            Then("ownerId should be the message key") {
                ownerCreatedEvent.should {
                    it.asKeyValue().key == ownerCreatedEvent.getOwnerId()
                }
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

    Given("owner updated events") {

        When("an ownerId and name is specified") {
            val ownerUpdatesEvent = ownerUpdated {
                ownerId = "1"
                name = "nay name"
            }

            Then("an event should be created") {
                ownerUpdatesEvent.shouldNotBeNull()
            }

            Then("ownerId should be the message key") {
                ownerUpdatesEvent.should {
                    it.asKeyValue().key == ownerUpdatesEvent.getOwnerId()
                }
            }
        }

        When("only the owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    ownerUpdated {
                        ownerId = "1"
                    }
                }
            }
        }
    }

    Given("owner deleted events") {
        When("an ownerId is specified") {
            val ownerDeletedEvent = ownerDeleted {
                ownerId = "1"
            }

            Then("an event should be created") {
                ownerDeletedEvent.shouldNotBeNull()
            }

            Then("ownerId should be the message key") {
                ownerDeletedEvent.should {
                    it.asKeyValue().key == ownerDeletedEvent.getOwnerId()
                }
            }
        }

        When("ono owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    ownerDeleted {}
                }
            }
        }
    }
})
