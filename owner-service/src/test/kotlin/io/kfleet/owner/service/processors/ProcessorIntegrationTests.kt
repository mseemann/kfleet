package io.kfleet.owner.service.processors


import io.kfleet.commands.CommandStatus
import io.kfleet.common.configuration.ObjectMapperConfig
import io.kfleet.common.customRetry
import io.kfleet.owner.service.WebConfiguration
import io.kfleet.owner.service.domain.CarModel
import io.kfleet.owner.service.repos.*
import io.kfleet.owner.service.web.NewCar
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testing.KafkaContextInitializer
import kotlin.test.*

@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
@Import(ObjectMapperConfig::class, WebConfiguration::class)
class ProcessorIntegrationTests {

    @Autowired
    lateinit var repo: OwnerRepository

    @Autowired
    lateinit var commandsResponseRepository: CommandsResponseRepository


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

        val newName = "test2"
        val updateOwnerNameParams = UpdateOwnerParams(ownerId = ownerId, ownerName = newName)

        val updateCommand = repo.submitUpdateOwnerNameCommand(updateOwnerNameParams).block()
        assertNotNull(updateCommand)


        val updateCommandResponse = commandsResponseRepository
                .findCommandResponse(updateCommand.getCommandId())
                .customRetry()
                .block()
        assertNotNull(updateCommandResponse)
        assertEquals(CommandStatus.SUCCEEDED, updateCommandResponse.getStatus())

        val updatedOwner = repo
                .findById(updateCommandResponse.getRessourceId())
                .customRetry().block()
        assertNotNull(updatedOwner)
        expect(newName) { updatedOwner.getName() }
    }

    @Test
    fun submitCreateOwnerCommandRejected() {
        val ownerName = "test2"
        val ownerId = "2"
        val createOwnerParams = CreateOwnerParams(ownerId = ownerId, ownerName = ownerName)

        val commandSucceeded = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(commandSucceeded)

        val commandRejected = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(commandRejected)

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
    fun submitDeleteOwnerCommand() {
        val ownerName = "test"
        val ownerId = "1"
        val createOwnerParams = CreateOwnerParams(ownerId = ownerId, ownerName = ownerName)

        val command = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(command)

        repo.findById(ownerId).customRetry().block()


        val deleteOwnerCommand = DeleteOwnerParams(ownerId = ownerId)

        val commandDelete = repo.submitDeleteOwnerCommand(deleteOwnerCommand).block()
        assertNotNull(commandDelete)
        expect(ownerId) { commandDelete.getOwnerId() }


        val commandDeleteResponse = commandsResponseRepository
                .findCommandResponse(commandDelete.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandDeleteResponse)
        assertEquals(CommandStatus.SUCCEEDED, commandDeleteResponse.getStatus())
        assertEquals(ownerId, commandDeleteResponse.getRessourceId())
        assertNotEquals(ownerId, commandDeleteResponse.getCommandId())

    }

    @Test
    fun submitRegisterAnDeregisterCarCommand() {
        val ownerName = "test"
        val ownerId = "3"
        val createOwnerParams = CreateOwnerParams(ownerId = ownerId, ownerName = ownerName)

        val command = repo.submitCreateOwnerCommand(createOwnerParams).block()
        assertNotNull(command)

        repo.findById(ownerId).customRetry().block()

        val newCar = NewCar(CarModel.ModelX)
        val registerCarCommand = RegisterCarParams(ownerId = ownerId, newCar = newCar)

        val commandRegisterCar = repo.submitRegisterCarCommand(registerCarCommand).block()
        assertNotNull(commandRegisterCar)
        expect(ownerId) { commandRegisterCar.getOwnerId() }

        val commandResponse = commandsResponseRepository
                .findCommandResponse(commandRegisterCar.getCommandId())
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
        expect(1) { owner.getCars().size }

        val deregisterCarCommand = DeregisterCarParams(ownerId = ownerId, carId = owner.getCars().get(0).getId())
        val commandDeregisterCar = repo.submitDeregisterCarCommand(deregisterCarCommand).block()
        assertNotNull(commandDeregisterCar)
        expect(ownerId) { commandDeregisterCar.getOwnerId() }

        val commandResponseDereg = commandsResponseRepository
                .findCommandResponse(commandDeregisterCar.getCommandId())
                .customRetry()
                .block()
        assertNotNull(commandResponseDereg)
        assertEquals(CommandStatus.SUCCEEDED, commandResponseDereg.getStatus())

        val ownerDeregisteredCar = repo
                .findById(commandResponseDereg.getRessourceId())
                .customRetry().block()
        assertNotNull(ownerDeregisteredCar)
        expect(0) { ownerDeregisteredCar.getCars().size }
    }
}


