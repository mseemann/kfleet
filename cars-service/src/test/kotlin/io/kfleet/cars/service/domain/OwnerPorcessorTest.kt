package io.kfleet.cars.service.domain

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.processors.CommandAndOwner
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.expect


class OwnerPorcessorTest {


    private val ownerProcessor = OwnerProcessor()

    @Test
    fun createOwnerRejectedTest() {
        val ownerId = "1"

        val command = CreateOwnerCommand.newBuilder().apply {
            commandId = ownerId
            setOwnerId(ownerId)
            name = "test"
        }.build()

        val owner = Owner.newBuilder().apply {
            id = ownerId
            name = "test"
        }.build()

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

        val command = CreateOwnerCommand.newBuilder().apply {
            commandId = "65823"
            setOwnerId(ownerId)
            name = ownerName
        }.build()

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
}
