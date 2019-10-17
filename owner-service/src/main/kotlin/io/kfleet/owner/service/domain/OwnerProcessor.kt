package io.kfleet.owner.service.domain

import io.kfleet.commands.CommandStatus
import io.kfleet.domain.asKeyValue
import io.kfleet.domain.commandResponse
import io.kfleet.domain.events.*
import io.kfleet.owner.service.commands.*
import io.kfleet.owner.service.processors.CommandAndOwner
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component
import java.util.*
import kotlin.reflect.full.findAnnotation

private val log = KotlinLogging.logger {}

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class OwnerCommand

fun SpecificRecord.isOwnerCommand(): Boolean {
    return this::class.findAnnotation<OwnerCommand>() != null
}

fun owner(buildOwner: Owner.Builder.() -> Unit): Owner =
        Owner.newBuilder().apply { buildOwner() }.build()

fun Owner.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getId(), this)
}

fun car(buildCar: Car.Builder.() -> Unit): Car =
        Car.newBuilder().apply { buildCar() }.build()

fun createOwnerCommand(buildCreateOwnerCommand: CreateOwnerCommand.Builder.() -> Unit): CreateOwnerCommand =
        CreateOwnerCommand.newBuilder().apply { buildCreateOwnerCommand() }.build()

fun updateOwnerNameCommand(buildUpdateOwnerCommand: UpdateOwnerNameCommand.Builder.() -> Unit): UpdateOwnerNameCommand =
        UpdateOwnerNameCommand.newBuilder().apply { buildUpdateOwnerCommand() }.build()

fun deleteOwnerCommand(buildDeleteOwnerCommand: DeleteOwnerCommand.Builder.() -> Unit): DeleteOwnerCommand =
        DeleteOwnerCommand.newBuilder().apply { buildDeleteOwnerCommand() }.build()

fun registerCarCommand(buildRegisterCarCommand: RegisterCarCommand.Builder.() -> Unit): RegisterCarCommand =
        RegisterCarCommand.newBuilder().apply { buildRegisterCarCommand() }.build()

fun deregisterCarCommand(buildDeregisterCarCommand: DeregisterCarCommand.Builder.() -> Unit): DeregisterCarCommand =
        DeregisterCarCommand.newBuilder().apply { buildDeregisterCarCommand() }.build()


@Component
class OwnerProcessor {

    fun processCommand(commandAndOwner: CommandAndOwner): List<KeyValue<String, SpecificRecord?>> {

        log.debug { "processCommand: $commandAndOwner" }

        return when (val command = commandAndOwner.command) {
            is CreateOwnerCommand -> createOwner(command, commandAndOwner.owner)
            is UpdateOwnerNameCommand -> updateOwner(command, commandAndOwner.owner)
            is DeleteOwnerCommand -> deleteOwner(command, commandAndOwner.owner)
            is RegisterCarCommand -> registerCar(command, commandAndOwner.owner)
            is DeregisterCarCommand -> deregisterCar(command, commandAndOwner.owner)
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

                    ownerCreatedEvent {
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

    private fun updateOwner(command: UpdateOwnerNameCommand, owner: Owner?): List<KeyValue<String, SpecificRecord?>> {
        return if (owner == null) {
            listOf(responseOwnerNotExist(command.getCommandId(), command.getOwnerId()))
        } else {
            listOf(
                    owner {
                        id = owner.getId()
                        name = command.getName()
                    }.asKeyValue(),

                    ownerUpdatedEvent {
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

                    ownerDeletedEvent {
                        ownerId = command.getOwnerId()
                    }.asKeyValue(),


                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getOwnerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            ).plus(
                    owner.getCars().map {
                        carDeregisteredEvent {
                            carId = it.getId()
                        }.asKeyValue()
                    }
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

    private fun responseCarNotExist(commandId: String, carId: String): KeyValue<String, SpecificRecord?> {
        return commandResponse {
            setCommandId(commandId)
            ressourceId = null
            status = CommandStatus.REJECTED
            reason = "Car with id $carId did not exist"
        }.asKeyValue()
    }

    private fun registerCar(command: RegisterCarCommand, owner: Owner?): List<KeyValue<String, SpecificRecord?>> {
        return if (owner == null) {
            listOf(responseOwnerNotExist(command.getCommandId(), command.getOwnerId()))
        } else {
            val newCar = car {
                id = UUID.randomUUID().toString()
                type = command.getType()
            }

            owner.setCars(owner.getCars().plus(newCar))

            listOf(
                    owner.asKeyValue(),

                    carRegisteredEvent {
                        carId = newCar.getId()
                    }.asKeyValue(),


                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getOwnerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }

    private fun deregisterCar(command: DeregisterCarCommand, owner: Owner?): List<KeyValue<String, SpecificRecord?>> {
        return if (owner == null) {
            listOf(responseOwnerNotExist(command.getCommandId(), command.getOwnerId()))
        } else {

            val carToDeregister = owner.getCars().filter { it.getId() == command.getCarId() }.firstOrNull()
                    ?: return listOf(responseCarNotExist(command.getCommandId(), command.getCarId()))

            owner.setCars(owner.getCars().minus(carToDeregister))

            listOf(
                    owner.asKeyValue(),

                    carDeregisteredEvent {
                        carId = command.getCarId()
                    }.asKeyValue(),

                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getOwnerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }
}
