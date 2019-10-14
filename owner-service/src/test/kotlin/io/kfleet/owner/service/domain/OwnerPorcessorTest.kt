package io.kfleet.owner.service.domain

import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.events.OwnerDeletedEvent
import io.kfleet.cars.service.events.OwnerUpdatedEvent
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.domain.events.ownerCreated
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
    fun deleteOwnerNameRejectedTest() {
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
        }

        val result = ownerProcessor.processCommand(CommandAndOwner(command, owner))

        expect(3, { result.count() })

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
}
