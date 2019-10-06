package io.kfleet.cars.service.processors

import io.kfleet.cars.service.domain.CarFactory
import io.kfleet.cars.service.repos.CarsRepository
import io.kfleet.cars.service.repos.CommandsResponseRepository
import io.kfleet.cars.service.repos.CreateOwnerParams
import io.kfleet.cars.service.repos.OwnerRepository
import io.kfleet.cars.service.simulation.CarsOutBindings
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import io.kfleet.common.headers
import org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testing.KafkaContextInitializer
import kotlin.test.*

@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class KafkaTest {

    @Autowired
    lateinit var repo: OwnerRepository

    @Autowired
    lateinit var commandsResponseRepository: CommandsResponseRepository

    @Autowired
    @Output(CarsOutBindings.CARS)
    lateinit var carOuputChannel: MessageChannel

    @Autowired
    lateinit var carsRepository: CarsRepository


    @Test
    fun submitCreateOwnerCommand() {
        val ownerName = "test"
        val ownerId = "1"
        val createOwnerParams = CreateOwnerParams(ownerId = ownerId, ownerName = ownerName)

        val command = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(command)
        expect(ownerId) { command.getOwnerId() }
        expect(ownerName) { command.getName() }

        val commandResponse = commandsResponseRepository
                .findCommandResponse(command.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandResponse)
        assertEquals(CommandStatus.SUCCEEDED, commandResponse.getStatus())
        assertEquals(ownerId, commandResponse.getRessourceId())
        assertNotEquals(ownerId, commandResponse.getCommandId())

        val owner = repo
                .findById(commandResponse.getRessourceId())
                .customRetry().block()
        assertNotNull(owner)
        expect(ownerName) { owner.getName() }
    }

    @Test
    fun submitCreateOwnerCommandRejected() {
        val ownerName = "test2"
        val ownerId = "2"
        val createOwnerParams = CreateOwnerParams(ownerId = ownerId, ownerName = ownerName)

        val commandSucceeded = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(commandSucceeded)
        println(commandSucceeded)

        // This is a huge problem: it is not sufficient to submit commands with a specific id
        // it is mandatory to wait for the owner creation process to happen to be safe that
        // the next command did not create an owner
        // So we have a todo: reject a second creation command with the same commandId (e.g. the ownerId)
//        val commandResponseSucceeded = commandsResponseRepository
//                .findCommandResponse(commandSucceeded.getCommandId())
//                .customRetry()
//                .block()
//        assertNotNull(commandResponseSucceeded)

        val commandRejected = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(commandRejected)
        println(commandRejected)

        val commandResponse = commandsResponseRepository
                .findCommandResponse(commandRejected.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandResponse)
        assertEquals(CommandStatus.REJECTED, commandResponse.getStatus())
        assertNull(commandResponse.getRessourceId())
        assertNotEquals(ownerId, commandResponse.getCommandId())
        assertEquals("Owner with id $ownerId already exists", commandResponse.getReason())

    }

    @Test
    fun submitCar() {

        val stats = carsRepository.getCarsStateCounts().block()
        assertNotNull(stats)
        expect(0) { stats.size }

        val carId = 1
        val car = CarFactory.createRandom(carId)
        val message = MessageBuilder.createMessage(car, headers(carId))
        val sended = carOuputChannel.send(message)
        // this must always be true - because for this output sync is false - e.g. not configured to be sync
        assert(true) { sended }

        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {

            val statsAfterPushMsg = carsRepository.getCarsStateCounts().block()!!

            expect(1L) { statsAfterPushMsg.get(car.getState().toString()) }
        }

    }
}


