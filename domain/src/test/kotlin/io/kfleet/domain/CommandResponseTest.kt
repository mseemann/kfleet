package io.kfleet.domain

import io.kfleet.commands.CommandStatus
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.BehaviorSpec
import org.apache.avro.AvroMissingFieldException

class CommandResponseTest : BehaviorSpec({

    Given("command responses") {

        When("a commandId, ressourceId and a status is defined") {
            val commandResponse = commandResponse {
                commandId = "1"
                ressourceId = "2"
                status = CommandStatus.SUCCEEDED
            }

            Then("an command repsonse should be created") {
                commandResponse.shouldNotBeNull()
            }

            Then("commandId should be the message key") {
                commandResponse.should {
                    it.asKeyValue().key == commandResponse.getCommandId()
                }
            }
        }

        When("only the command id is given") {
            Then("an exception should be thrown") {
                shouldThrow<AvroMissingFieldException> {
                    commandResponse {
                        commandId = "1"
                    }
                }
            }
        }
    }
})
