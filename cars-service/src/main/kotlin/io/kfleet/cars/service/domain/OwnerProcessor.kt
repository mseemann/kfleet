package io.kfleet.cars.service.domain

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.DeleteOwnerCommand
import io.kfleet.cars.service.commands.UpdateOwnernameCommand
import io.kfleet.cars.service.processors.CommandAndOwner
import io.kfleet.commands.CommandStatus
import io.kfleet.domain.asKeyValue
import io.kfleet.domain.commandResponse
import io.kfleet.domain.events.asKeyValue
import io.kfleet.domain.events.ownerCreated
import io.kfleet.domain.events.ownerDeleted
import io.kfleet.domain.events.ownerUpdated
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

fun Owner.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getId(), this)
}

inline fun createOwnerCommand(buildCreateOwnerCommand: CreateOwnerCommand.Builder.() -> Unit): CreateOwnerCommand {
    val builder = CreateOwnerCommand.newBuilder()
    builder.buildCreateOwnerCommand()
    return builder.build()
}

inline fun updateOwnerNameCommand(buildUpdateOwnerCommand: UpdateOwnernameCommand.Builder.() -> Unit): UpdateOwnernameCommand {
    val builder = UpdateOwnernameCommand.newBuilder()
    builder.buildUpdateOwnerCommand()
    return builder.build()
}

inline fun deleteOwnerCommand(buildDeleteOwnerCommand: DeleteOwnerCommand.Builder.() -> Unit): DeleteOwnerCommand {
    val builder = DeleteOwnerCommand.newBuilder()
    builder.buildDeleteOwnerCommand()
    return builder.build()
}

@Component
class OwnerProcessor {

    fun processCommand(commandAndOwner: CommandAndOwner): List<KeyValue<String, SpecificRecord?>> {

        log.debug { "processCommand: $commandAndOwner" }

        return when (val command = commandAndOwner.command) {
            is CreateOwnerCommand -> createOwner(command, commandAndOwner.owner)
            is UpdateOwnernameCommand -> updateOwner(command, commandAndOwner.owner)
            is DeleteOwnerCommand -> deleteOwner(command, commandAndOwner.owner)
            else -> throw RuntimeException("unsupported command: ${command::class}")
        }

    }

    private fun createOwner(command: CreateOwnerCommand, owner: Owner?): List<KeyValue<String, SpecificRecord?>> {

        return if (owner != null) {
            listOf(
                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = null
                        status = CommandStatus.REJECTED
                        reason = "Owner with id ${owner.getId()} already exists"
                    }.asKeyValue()
            )
        } else {
            listOf(
                    owner {
                        id = command.getOwnerId()
                        name = command.getName()
                    }.asKeyValue(),

                    ownerCreated {
                        ownerId = command.getOwnerId()
                        name = command.getName()
                    }.asKeyValue(),

                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getOwnerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }

    private fun updateOwner(command: UpdateOwnernameCommand, owner: Owner?): List<KeyValue<String, SpecificRecord?>> {
        return if (owner == null) {
            listOf(responseOwnerNotExist(command.getCommandId(), command.getOwnerId()))
        } else {
            listOf(
                    owner {
                        id = command.getOwnerId()
                        name = command.getName()
                    }.asKeyValue(),

                    ownerUpdated {
                        ownerId = command.getOwnerId()
                        name = command.getName()
                    }.asKeyValue(),


                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getOwnerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }


    private fun deleteOwner(command: DeleteOwnerCommand, owner: Owner?): List<KeyValue<String, SpecificRecord?>> {
        return if (owner == null) {
            listOf(responseOwnerNotExist(command.getCommandId(), command.getOwnerId()))
        } else {
            listOf(
                    KeyValue<String, SpecificRecord?>(command.getOwnerId(), null),

                    ownerDeleted {
                        ownerId = command.getOwnerId()
                    }.asKeyValue(),


                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getOwnerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }

    private fun responseOwnerNotExist(commandId: String, ownerId: String): KeyValue<String, SpecificRecord?> {
        return commandResponse {
            setCommandId(commandId)
            ressourceId = null
            status = CommandStatus.REJECTED
            reason = "Owner with id $ownerId did not exist"
        }.asKeyValue()
    }
}
