package io.kfleet.cars.service.domain

import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.processors.CommandAndOwner
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import mu.KotlinLogging
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class OwnerProcessor {

    fun createOwner(ownerId: String, commandAndOwner: CommandAndOwner): MutableList<KeyValue<String, Any>> {

        log.debug { "$ownerId -> $commandAndOwner" }

        val result = mutableListOf<KeyValue<String, Any>>()

        val command = commandAndOwner.command

        if (commandAndOwner.owner != null) {

            val response = CommandResponse.newBuilder().apply {
                commandId = command.getCommandId()
                ressourceId = null
                status = CommandStatus.REJECTED
                reason = "Owner with id $ownerId already exists"
            }.build()
            result.add(KeyValue(command.getCommandId(), response))

        } else {

            val owner = Owner.newBuilder().apply {
                id = ownerId
                name = command.getName()
            }.build()
            result.add(KeyValue(ownerId, owner))

            val ownerCreatedEvents = OwnerCreatedEvent.newBuilder().apply {
                setOwnerId(command.getOwnerId())
                name = command.getName()
            }.build()
            result.add(KeyValue(ownerId, ownerCreatedEvents))

            val response = CommandResponse.newBuilder().apply {
                commandId = command.getCommandId()
                ressourceId = ownerId
                status = CommandStatus.SUCCEEDED
            }.build()
            result.add(KeyValue(command.getCommandId(), response))
        }

        return result
    }
}
