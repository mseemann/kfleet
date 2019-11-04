package io.kfleet.traveler.service.domain

import io.kfleet.commands.CommandStatus
import io.kfleet.domain.asKeyValue
import io.kfleet.domain.commandResponse
import io.kfleet.domain.events.asKeyValue
import io.kfleet.domain.events.rideRequestedEvent
import io.kfleet.domain.events.travelerCreatedEvent
import io.kfleet.domain.events.travelerDeletedEvent
import io.kfleet.geo.QuadTree
import io.kfleet.traveler.service.commands.CarRequestCommand
import io.kfleet.traveler.service.commands.CreateTravelerCommand
import io.kfleet.traveler.service.commands.DeleteTravelerCommand
import io.kfleet.traveler.service.processors.CommandAndTraveler
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component
import java.util.*
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

fun carRequestCommand(buildCarRequestCommand: CarRequestCommand.Builder.() -> Unit): CarRequestCommand =
        CarRequestCommand.newBuilder().apply { buildCarRequestCommand() }.build()

@Component
class TravelerProcessor {

    fun processCommand(commandAndTraveler: CommandAndTraveler): List<KeyValue<String, SpecificRecord?>> {

        log.debug { "processCommand: $commandAndTraveler" }

        return when (val command = commandAndTraveler.command) {
            is CreateTravelerCommand -> createTraveler(command, commandAndTraveler.traveler)
            is DeleteTravelerCommand -> deleteTraveler(command, commandAndTraveler.traveler)
            is CarRequestCommand -> requestACar(command, commandAndTraveler.traveler)
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

    private fun requestACar(command: CarRequestCommand, traveler: Traveler?): List<KeyValue<String, SpecificRecord?>> {
        return if (traveler == null) {
            listOf(responseTravelerNotExist(command.getCommandId(), command.getTravelerId()))
        } else {
            // TODO do we need to store ride requests at traveler level - or at least open ride request

            // send a riderequest to each geoindex quadrant
            val matchingGeoIndexes = QuadTree.getIntersectingIndexes(
                    lng = command.getFrom().getLng(),
                    lat = command.getFrom().getLat(),
                    withDistanceInKilometers = 5.0).getIndexPaths()
            val requestGroupId = UUID.randomUUID().toString()

            val requests = matchingGeoIndexes.map {
                rideRequestedEvent {
                    requestId = UUID.randomUUID().toString()
                    setRequestGroupId(requestGroupId)
                    travelerId = command.getTravelerId()
                    from = command.getFrom().toGeoPositionRideRequest()
                    fromGeoIndex = it
                    to = command.getTo().toGeoPositionRideRequest()
                    requestTime = command.getRequestTime()
                }.asKeyValue()
            }

            listOf(
                    commandResponse {
                        commandId = command.getCommandId()
                        ressourceId = command.getTravelerId()
                        status = CommandStatus.SUCCEEDED
                    }.asKeyValue()
            ).plus(requests)
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
