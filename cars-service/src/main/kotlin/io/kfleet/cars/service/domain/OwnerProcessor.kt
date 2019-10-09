package io.kfleet.cars.service.domain

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.UpdateOwnernameCommand
import io.kfleet.cars.service.processors.CommandAndOwner
import io.kfleet.commands.CommandStatus
import io.kfleet.domain.asKeyValue
import io.kfleet.domain.commandResponse
import io.kfleet.domain.events.asKeyValue
import io.kfleet.domain.events.ownerCreated
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component
import kotlin.reflect.full.findAnnotation

private val log = KotlinLogging.logger {}

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class OwnerCommand

fun SpecificRecord.isOwnerCommand(): Boolean {
    return this::class.findAnnotation<OwnerCommand>() != null
}

inline fun owner(buildOwner: Owner.Builder.() -> Unit): Owner {
    val builder = Owner.newBuilder()
    builder.buildOwner()
    return builder.build()
}

fun Owner.asKeyValue(): KeyValue<String, SpecificRecord> {
    return KeyValue(this.getId(), this)
}

fun KeyValue<String, SpecificRecord>.addTo(kv: MutableList<KeyValue<String, SpecificRecord>>) {
    kv.add(this)
}

@Component
class OwnerProcessor {

    fun processCommand(commandAndOwner: CommandAndOwner): List<KeyValue<String, SpecificRecord>> {

        log.debug { "$commandAndOwner" }

        return when (val command = commandAndOwner.command) {
            is CreateOwnerCommand -> createOwner(command, commandAndOwner.owner)
            is UpdateOwnernameCommand -> updateOwner(command, commandAndOwner.owner)
            else -> throw RuntimeException("unsupported command ${command::class}")
        }

    }

    fun createOwner(command: CreateOwnerCommand, owner: Owner?): List<KeyValue<String, SpecificRecord>> {
        val result = mutableListOf<KeyValue<String, SpecificRecord>>()
        if (owner != null) {

            commandResponse {
                commandId = command.getCommandId()
                ressourceId = null
                status = CommandStatus.REJECTED
                reason = "Owner with id ${owner.getId()} already exists"
            }.asKeyValue().addTo(result)

        } else {

            owner {
                id = command.getOwnerId()
                name = command.getName()
            }.asKeyValue().addTo(result)

            ownerCreated {
                ownerId = command.getOwnerId()
                name = command.getName()
            }.asKeyValue().addTo(result)

            commandResponse {
                commandId = command.getCommandId()
                ressourceId = command.getOwnerId()
                status = CommandStatus.SUCCEEDED
            }.asKeyValue().addTo(result)
        }

        return result
    }

    fun updateOwner(command: UpdateOwnernameCommand, owner: Owner?): List<KeyValue<String, SpecificRecord>> {
        val result = mutableListOf<KeyValue<String, SpecificRecord>>()

        return result
    }
}
