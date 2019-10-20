package io.kfleet.traveler.service.domain

import io.kfleet.commands.CommandStatus
import io.kfleet.domain.asKeyValue
import io.kfleet.domain.commandResponse
import io.kfleet.domain.events.asKeyValue
import io.kfleet.domain.events.travelerCreatedEvent
import io.kfleet.domain.events.travelerDeletedEvent
import io.kfleet.traveler.service.commands.CreateTravelerCommand
import io.kfleet.traveler.service.commands.DeleteTravelerCommand
import io.kfleet.traveler.service.processors.CommandAndTraveler
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component
import kotlin.reflect.full.findAnnotation

private val log = KotlinLogging.logger {}

@Retention(AnnotationRetention.RUNTIME)
@Target((AnnotationTarget.CLASS))
annotation class TravelerCommand

fun SpecificRecord.isTravelerCommand(): Boolean {
    return this::class.findAnnotation<TravelerCommand>() != null
}

fun traveler(buildTraveler: Traveler.Builder.() -> Unit): Traveler =
        Traveler.newBuilder().apply { buildTraveler() }.build()

fun Traveler.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getId(), this)
}


fun createTravelerCommand(buildCreateTravelerCommand: CreateTravelerCommand.Builder.() -> Unit): CreateTravelerCommand =
        CreateTravelerCommand.newBuilder().apply { buildCreateTravelerCommand() }.build()

fun deleteTravelerCommand(buildDeleteTravelerCommand: DeleteTravelerCommand.Builder.() -> Unit): DeleteTravelerCommand =
        DeleteTravelerCommand.newBuilder().apply { buildDeleteTravelerCommand() }.build()


@Component
class TravelerProcessor {

    fun processCommand(commandAndTraveler: CommandAndTraveler): List<KeyValue<String, SpecificRecord?>> {

        log.debug { "processCommand: $commandAndTraveler" }

        return when (val command = commandAndTraveler.command) {
            is CreateTravelerCommand -> createTraveler(command, commandAndTraveler.traveler)
            is DeleteTravelerCommand -> deleteTraveler(command, commandAndTraveler.traveler)
            else -> throw RuntimeException("unsupported command: ${command::class}")
        }

    }

    private fun createTraveler(command: CreateTravelerCommand, traveler: Traveler?): List<KeyValue<String, SpecificRecord?>> {

        return if (traveler != null) {
            listOf(
                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = null
                        status = CommandStatus.REJECTED
                        reason = "Traveler with id ${traveler.getId()} already exists"
                    }.asKeyValue()
            )
        } else {
            listOf(
                    traveler {
                        id = command.getTravelerId()
                        name = command.getName()
                        email = command.getEmail()
                    }.asKeyValue(),

                    travelerCreatedEvent {
                        travelerId = command.getTravelerId()
                        name = command.getName()
                        email = command.getEmail()
                    }.asKeyValue(),

                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getTravelerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }

    private fun deleteTraveler(command: DeleteTravelerCommand, traveler: Traveler?): List<KeyValue<String, SpecificRecord?>> {
        return if (traveler == null) {
            listOf(responseTravelerNotExist(command.getCommandId(), command.getTravelerId()))
        } else {
            listOf(
                    KeyValue<String, SpecificRecord?>(command.getTravelerId(), null),

                    travelerDeletedEvent {
                        travelerId = command.getTravelerId()
                    }.asKeyValue(),


                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getTravelerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            )
        }
    }

    private fun responseTravelerNotExist(commandId: String, travelerId: String): KeyValue<String, SpecificRecord?> {
        return commandResponse {
            setCommandId(commandId)
            ressourceId = null
            status = CommandStatus.REJECTED
            reason = "Traveler with id $travelerId did not exist"
        }.asKeyValue()
    }
}
