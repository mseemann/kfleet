package io.kfleet.traveler.service.processors


import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import io.kfleet.traveler.service.repos.CommandsResponseRepository
import io.kfleet.traveler.service.repos.TravelerRepository
import io.kfleet.traveler.service.web.DeleteTravelerParams
import io.kfleet.traveler.service.web.NewTraveler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testing.KafkaContextInitializer
import kotlin.test.*

@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class ProcessorIntegrationTests {

    @Autowired
    lateinit var repo: TravelerRepository

    @Autowired
    lateinit var commandsResponseRepository: CommandsResponseRepository


    @Test
    fun submitCreateTravelerCommand() {
        val travelerName = "test"
        val travelerEmail = "a@a.com"
        val travelerId = "1"
        val createTravelerParams = NewTraveler(
                travelerId = travelerId,
                name = travelerName,
                email = travelerEmail)

        val command = repo.submitCreateTravelerCommand(createTravelerParams).block()
        assertNotNull(command)
        expect(travelerId) { command.getTravelerId() }
        expect(travelerName) { command.getName() }
        expect(travelerEmail) { command.getEmail() }

        val commandResponse = commandsResponseRepository
                .findCommandResponse(command.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandResponse)
        assertEquals(CommandStatus.SUCCEEDED, commandResponse.getStatus())
        assertEquals(travelerId, commandResponse.getRessourceId())
        assertNotEquals(travelerId, commandResponse.getCommandId())

        val traveler = repo
                .findById(commandResponse.getRessourceId())
                .customRetry().block()
        assertNotNull(traveler)
        expect(travelerName) { traveler.getName() }
    }

    @Test
    fun submitCreateTravelerCommandRejected() {
        val travelerId = "2"
        val createTravelerParams = NewTraveler(
                travelerId = travelerId,
                name = "test2",
                email = "test@a.com")

        val commandSucceeded = repo.submitCreateTravelerCommand(createTravelerParams).block()
        assertNotNull(commandSucceeded)

        val commandRejected = repo.submitCreateTravelerCommand(createTravelerParams).block()
        assertNotNull(commandRejected)

        val commandResponse = commandsResponseRepository
                .findCommandResponse(commandRejected.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandResponse)
        assertEquals(CommandStatus.REJECTED, commandResponse.getStatus())
        assertNull(commandResponse.getRessourceId())
        assertNotEquals(travelerId, commandResponse.getCommandId())
        assertEquals("Traveler with id $travelerId already exists", commandResponse.getReason())

    }

    @Test
    fun submitDeleteTravelerCommand() {
        val travelerId = "1"
        val createTravelerParams = NewTraveler(
                travelerId = travelerId,
                name = "test",
                email = "a@a.com")

        val command = repo.submitCreateTravelerCommand(createTravelerParams).block()
        assertNotNull(command)

        repo.findById(travelerId).customRetry().block()

        val deleteTravelerCommand = DeleteTravelerParams(travelerId = travelerId)

        val commandDelete = repo.submitDeleteTravelerCommand(deleteTravelerCommand).block()
        assertNotNull(commandDelete)
        expect(travelerId) { commandDelete.getTravelerId() }


        val commandDeleteResponse = commandsResponseRepository
                .findCommandResponse(commandDelete.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandDeleteResponse)
        assertEquals(CommandStatus.SUCCEEDED, commandDeleteResponse.getStatus())
        assertEquals(travelerId, commandDeleteResponse.getRessourceId())
        assertNotEquals(travelerId, commandDeleteResponse.getCommandId())

    }


}


