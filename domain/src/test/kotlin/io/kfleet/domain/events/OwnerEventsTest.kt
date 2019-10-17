package io.kfleet.domain.events

import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroMissingFieldException

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
                ownerCreatedEvent.should {
                    it.asKeyValue().key == ownerCreatedEvent.getOwnerId()
                }
            }
        }

        When("only the owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
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
                ownerUpdatesEvent.should {
                    it.asKeyValue().key == ownerUpdatesEvent.getOwnerId()
                }
            }
        }

        When("only the owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
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
                    it.asKeyValue().key == ownerDeletedEvent.getOwnerId()
                }
            }
        }

        When("only owner id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    ownerDeletedEvent {}
                }
            }
        }
    }

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
                    it.asKeyValue().key == carRegisteredEvent.getCarId()
                }
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
                    it.asKeyValue().key == carDeregisteredEvent.getCarId()
                }
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
