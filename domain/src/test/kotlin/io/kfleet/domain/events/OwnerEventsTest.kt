package io.kfleet.domain.events

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroRuntimeException

class OwnerEventsTest : BehaviorSpec({

    Given("owner created events") {

        When("an ownerId and name is specified") {
            val ownerCreatedEvent = ownerCreatedEvent {
                ownerId = "1"
                name = "nay name"
            }

            Then("an event should be created") {
                ownerCreatedEvent.shouldNotBeNull()
            }

            Then("ownerId should be the message key") {
                ownerCreatedEvent.asKeyValue().key.shouldBe(ownerCreatedEvent.getOwnerId())
            }

            Then("the 'isOwnerEvent' method should return true") {
                ownerCreatedEvent.isOwnerEvent().shouldBeTrue()
            }
        }

        When("only the owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    ownerCreatedEvent {
                        ownerId = "1"
                    }
                }
            }
        }
    }

    Given("owner updated events") {

        When("an ownerId and name is specified") {
            val ownerUpdatesEvent = ownerUpdatedEvent {
                ownerId = "1"
                name = "nay name"
            }

            Then("an event should be created") {
                ownerUpdatesEvent.shouldNotBeNull()
            }

            Then("ownerId should be the message key") {
                ownerUpdatesEvent.asKeyValue().key.shouldBe(ownerUpdatesEvent.getOwnerId())
            }

            Then("the 'isOwnerEvent' method should return true") {
                ownerUpdatesEvent.isOwnerEvent().shouldBeTrue()
            }
        }

        When("only the owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    ownerUpdatedEvent {
                        ownerId = "1"
                    }
                }
            }
        }
    }

    Given("owner deleted events") {
        When("an ownerId is specified") {
            val ownerDeletedEvent = ownerDeletedEvent {
                ownerId = "1"
            }

            Then("an event should be created") {
                ownerDeletedEvent.shouldNotBeNull()
            }

            Then("ownerId should be the message key") {
                ownerDeletedEvent.should {
                    it.asKeyValue().key === ownerDeletedEvent.getOwnerId()
                }
            }

            Then("the 'isOwnerEvent' method should return true") {
                ownerDeletedEvent.isOwnerEvent().shouldBeTrue()
            }
        }

        When("only owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroRuntimeException> {
                    ownerDeletedEvent {}
                }
            }
        }
    }

})
