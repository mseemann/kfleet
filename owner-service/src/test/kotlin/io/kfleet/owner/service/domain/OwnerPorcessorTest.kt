package io.kfleet.owner.service.domain

import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.domain.events.ownerCreated
import io.kfleet.owner.service.events.*
import io.kfleet.owner.service.processors.CommandAndOwner
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.expect


class OwnerPorcessorTest {


    private val ownerProcessor = OwnerProcessor()

    @Test
    fun createOwnerRejectedTest() {
        val ownerId = "1"

        val command = createOwnerCommand {
            commandId = ownerId
            setOwnerId(ownerId)
            name = "test"
        }

        val owner = owner {
            id = ownerId
            name = "test"
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(1) { result.count() }
        expect(ownerId) { result[0].key }
        expect(CommandStatus.REJECTED) { (result[0].value as CommandResponse).getStatus() }
        assertNull((result[0].value as CommandResponse).getRessourceId())
        expect("Owner with id $ownerId already exists") { (result[0].value as CommandResponse).getReason() }
    }

    @Test
    fun createOwnerSucceededTest() {

        val ownerId = "1"
        val ownerName = "test"

        val command = createOwnerCommand {
            commandId = "65823"
            setOwnerId(ownerId)
            name = ownerName
        }

        val owner = null

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(3, { result.count() })

        val ownerKV = result.filter { it.value is Owner }
        expect(ownerId) { ownerKV[0].key }
        expect(ownerId) { (ownerKV[0].value as Owner).getId() }
        expect(ownerName) { (ownerKV[0].value as Owner).getName() }
        expect(emptyList()) { (ownerKV[0].value as Owner).getCars() }

        val commandKv = result.filter { it.value is CommandResponse }
        assertNotEquals(ownerId, commandKv[0].key)
        expect(CommandStatus.SUCCEEDED) { (commandKv[0].value as CommandResponse).getStatus() }
        expect(ownerId) { (commandKv[0].value as CommandResponse).getRessourceId() }
        assertNull((commandKv[0].value as CommandResponse).getReason())

        val eventKv = result.filter { it.value is OwnerCreatedEvent }
        expect(ownerId) { eventKv[0].key }
        expect(ownerName) { (eventKv[0].value as OwnerCreatedEvent).getName() }
    }


    @Test
    fun updateOwnerNameRejectedTest() {
        val ownerId = "1"

        val command = updateOwnerNameCommand {
            commandId = ownerId
            setOwnerId(ownerId)
            name = "test"
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, null))

        expect(1) { result.count() }
        expect(ownerId) { result[0].key }
        expect(CommandStatus.REJECTED) { (result[0].value as CommandResponse).getStatus() }
        assertNull((result[0].value as CommandResponse).getRessourceId())
        expect("Owner with id $ownerId did not exist") { (result[0].value as CommandResponse).getReason() }
    }

    @Test
    fun updateOwnerNameSucceededTest() {

        val ownerId = "1"
        val ownerName = "test"

        val command = updateOwnerNameCommand {
            commandId = "65823"
            setOwnerId(ownerId)
            name = ownerName
        }

        val owner = owner {
            id = ownerId
            name = "old name"
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(3, { result.count() })

        val ownerKV = result.filter { it.value is Owner }
        expect(ownerId) { ownerKV[0].key }
        expect(ownerId) { (ownerKV[0].value as Owner).getId() }
        expect(ownerName) { (ownerKV[0].value as Owner).getName() }
        expect(emptyList()) { (ownerKV[0].value as Owner).getCars() }

        val commandKv = result.filter { it.value is CommandResponse }
        assertNotEquals(ownerId, commandKv[0].key)
        expect(CommandStatus.SUCCEEDED) { (commandKv[0].value as CommandResponse).getStatus() }
        expect(ownerId) { (commandKv[0].value as CommandResponse).getRessourceId() }
        assertNull((commandKv[0].value as CommandResponse).getReason())

        val eventKv = result.filter { it.value is OwnerUpdatedEvent }
        expect(ownerId) { eventKv[0].key }
        expect(ownerName) { (eventKv[0].value as OwnerUpdatedEvent).getName() }
    }

    @Test
    fun deleteOwnerRejectedTest() {
        val ownerId = "1"

        val command = deleteOwnerCommand {
            commandId = ownerId
            setOwnerId(ownerId)
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, null))

        expect(1) { result.count() }
        expect(ownerId) { result[0].key }
        expect(CommandStatus.REJECTED) { (result[0].value as CommandResponse).getStatus() }
        assertNull((result[0].value as CommandResponse).getRessourceId())
        expect("Owner with id $ownerId did not exist") { (result[0].value as CommandResponse).getReason() }
    }

    @Test
    fun deleteOwnerSucceededTest() {

        val ownerId = "1"

        val command = deleteOwnerCommand {
            commandId = "65823"
            setOwnerId(ownerId)
        }

        val owner = owner {
            id = ownerId
            name = "name"
            cars = listOf(
                    car {
                        id = "1"
                    }
            )
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(4, { result.count() })

        val ownerKV = result.filter { it.value == null }
        expect(ownerId) { ownerKV[0].key }

        val commandKv = result.filter { it.value is CommandResponse }
        assertNotEquals(ownerId, commandKv[0].key)
        expect(CommandStatus.SUCCEEDED) { (commandKv[0].value as CommandResponse).getStatus() }
        expect(ownerId) { (commandKv[0].value as CommandResponse).getRessourceId() }
        assertNull((commandKv[0].value as CommandResponse).getReason())

        val eventKv = result.filter { it.value is OwnerDeletedEvent }
        expect(ownerId) { eventKv[0].key }
        expect(ownerId) { (eventKv[0].value as OwnerDeletedEvent).getOwnerId() }

        val deregisterCarEvent = result.filter { it.value is CarDeregisteredEvent }
        expect("1") { deregisterCarEvent[0].key }
        expect("1") { (deregisterCarEvent[0].value as CarDeregisteredEvent).getCarId() }
    }

    @Test
    fun testUnknownCommandException() {
        // this is not a owner command! did you see it?
        val command = ownerCreated {
            ownerId = "1"
            name = " name"
        }
        assertFailsWith(RuntimeException::class) {
            ownerProcessor.processCommand(CommandAndOwner(command, null))
        }
    }

    @Test
    fun registerCarSucceededTest() {
        val ownerId = "1"
        val carModel = CarModel.Model3

        val command = registerCarCommand {
            commandId = "65823"
            setOwnerId(ownerId)
            type = carModel
        }

        val owner = owner {
            id = ownerId
            name = "name"
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(3, { result.count() })

        val ownerKV = result.filter { it.value is Owner }
        expect(ownerId) { ownerKV[0].key }
        expect(ownerId) { (ownerKV[0].value as Owner).getId() }
        expect(1) { (ownerKV[0].value as Owner).getCars().size }
        expect(carModel) { (ownerKV[0].value as Owner).getCars()[0].getType() }

        val commandKv = result.filter { it.value is CommandResponse }
        assertNotEquals(ownerId, commandKv[0].key)
        expect(CommandStatus.SUCCEEDED) { (commandKv[0].value as CommandResponse).getStatus() }
        expect(ownerId) { (commandKv[0].value as CommandResponse).getRessourceId() }
        assertNull((commandKv[0].value as CommandResponse).getReason())

        val eventKv = result.filter { it.value is CarRegisteredEvent }
        expect(eventKv[0].key) { (eventKv[0].value as CarRegisteredEvent).getCarId() }
    }

    @Test
    fun registerCarRejectedTest() {
        val ownerId = "1"
        val commandId = "65823"

        val command = registerCarCommand {
            setCommandId(commandId)
            setOwnerId(ownerId)
            type = CarModel.Model3
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, null))

        expect(1) { result.count() }
        expect(commandId) { result[0].key }
        expect(CommandStatus.REJECTED) { (result[0].value as CommandResponse).getStatus() }
        assertNull((result[0].value as CommandResponse).getRessourceId())
        expect("Owner with id $ownerId did not exist") { (result[0].value as CommandResponse).getReason() }
    }

    @Test
    fun deregisterCarSucceededTest() {
        val ownerId = "1"
        val carId = "1"

        val command = deregisterCarCommand {
            commandId = "65823"
            setOwnerId(ownerId)
            setCarId(carId)
        }

        val owner = owner {
            id = ownerId
            name = "name"
            cars = listOf(
                    car {
                        id = carId
                        type = CarModel.ModelX
                    }
            )
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(3, { result.count() })

        val ownerKV = result.filter { it.value is Owner }
        expect(ownerId) { ownerKV[0].key }
        expect(ownerId) { (ownerKV[0].value as Owner).getId() }
        expect(0) { (ownerKV[0].value as Owner).getCars().size }

        val commandKv = result.filter { it.value is CommandResponse }
        assertNotEquals(ownerId, commandKv[0].key)
        expect(CommandStatus.SUCCEEDED) { (commandKv[0].value as CommandResponse).getStatus() }
        expect(ownerId) { (commandKv[0].value as CommandResponse).getRessourceId() }
        assertNull((commandKv[0].value as CommandResponse).getReason())

        val eventKv = result.filter { it.value is CarDeregisteredEvent }
        expect(eventKv[0].key) { (eventKv[0].value as CarDeregisteredEvent).getCarId() }
        expect(carId) { (eventKv[0].value as CarDeregisteredEvent).getCarId() }
    }

    @Test
    fun deregisterCarRejectedNoOwnerTest() {
        val ownerId = "1"
        val carId = "2"
        val commandId = "65823"

        val command = deregisterCarCommand {
            setCommandId(commandId)
            setOwnerId(ownerId)
            setCarId(carId)
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, null))

        expect(1) { result.count() }
        expect(commandId) { result[0].key }
        expect(CommandStatus.REJECTED) { (result[0].value as CommandResponse).getStatus() }
        assertNull((result[0].value as CommandResponse).getRessourceId())
        expect("Owner with id $ownerId did not exist") { (result[0].value as CommandResponse).getReason() }
    }


    @Test
    fun deregisterCarRejectedNoCarTest() {
        val ownerId = "1"
        val carId = "2"
        val commandId = "65823"

        val command = deregisterCarCommand {
            setCommandId(commandId)
            setOwnerId(ownerId)
            setCarId(carId)
        }

        val owner = owner {
            id = ownerId
            name = "name"
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(1) { result.count() }
        expect(commandId) { result[0].key }
        expect(CommandStatus.REJECTED) { (result[0].value as CommandResponse).getStatus() }
        assertNull((result[0].value as CommandResponse).getRessourceId())
        expect("Car with id $carId did not exist") { (result[0].value as CommandResponse).getReason() }
    }
}
