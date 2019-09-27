package io.kfleet.cars.service.processors

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.common.createSerdeWithAvroRegistry
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.WindowStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import java.time.Duration

data class CommandAndOwner(val command: CreateOwnerCommand, val owner: Owner?)

interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_COMMANDS_RESPONSE = "owner_commands_response"
        const val OWNER_EVENTS = "owner_events"
        const val OWNSERS = "owners"
        const val UNKNOW_COMMANDS = "unknown_owner_commands"
        const val OWNER_STORE = "owner-store"
        const val OWNER_COMMANDS_RESPONSE_STORE = "owner_commands_response_store"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, SpecificRecord>

    @Input(OWNSERS)
    fun inpuOwners(): KStream<String, Owner>

    @Input(OWNER_COMMANDS_RESPONSE)
    fun inputOwnerCommandResponses(): KStream<String, CommandResponse>
}


@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor(@Value("\${spring.cloud.stream.schema-registry-client.endpoint}") val endpoint: String) {

    private val ownerSerde by lazy { createSerdeWithAvroRegistry<Owner>(endpoint)() }
    private val commandResponseSerde by lazy { createSerdeWithAvroRegistry<CommandResponse>(endpoint)() }

    @StreamListener
    fun processCommands(
            @Input(OwnerCommandsProcessorBinding.OWNSERS) ownersStream: KStream<String, Owner>,
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS) commandStream: KStream<String, SpecificRecord>,
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE) commandResponseStream: KStream<String, CommandResponse>) {

        val ownerTable: KTable<String, Owner> = createOwnerKTable(ownersStream)
        createOwnerCommandResponseWindowedTable(commandResponseStream)


        val (createOwnerCommands, unknownOwnerCommands) = commandStream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value is CreateOwnerCommand },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownOwnerCommands.to(OwnerCommandsProcessorBinding.UNKNOW_COMMANDS)

        val joinedStream = createOwnerCommands
                .leftJoin(ownerTable) { command, owner -> CommandAndOwner(command = command as CreateOwnerCommand, owner = owner) }

        val (createOwnerStream, ownerNotCreatedStream) = joinedStream
                .branch(
                        Predicate<String, CommandAndOwner> { _, value -> value.owner == null },
                        Predicate<String, CommandAndOwner> { _, value -> value.owner !== null }
                )

        createOwnerStream
                .map { key, value ->
                    val owner = Owner.newBuilder().apply {
                        id = key
                        name = value.command.getName()
                    }.build()
                    KeyValue(key, owner)
                }
                .to(OwnerCommandsProcessorBinding.OWNSERS)

        createOwnerStream
                .map { key, value ->
                    val ownerCreatedEvents = OwnerCreatedEvent.newBuilder().apply {
                        ownerId = value.command.getOwnerId()
                        name = value.command.getName()
                    }.build()
                    KeyValue(key, ownerCreatedEvents)
                }
                .to(OwnerCommandsProcessorBinding.OWNER_EVENTS)

        createOwnerStream
                .map { _, value ->
                    val response = CommandResponse.newBuilder().apply {
                        commandId = value.command.getCommandId()
                        status = CommandStatus.SUCCEEDED
                    }.build()
                    KeyValue(value.command.getCommandId(), response)
                }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)

        ownerNotCreatedStream
                .map { key, value ->
                    val response = CommandResponse.newBuilder().apply {
                        commandId = value.command.getCommandId()
                        status = CommandStatus.REJECTED
                        reason = "Owner with id $key already exists"
                    }.build()
                    KeyValue(value.command.getCommandId(), response)
                }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)

    }

    /**
     * In the Car-Domain this is done with the Spring Boot Cloud Streams materializedAs Feature
     */
    private fun createOwnerKTable(ownersStream: KStream<String, Owner>): KTable<String, Owner> {

        val ownerStateStore =
                Materialized.`as`<String, Owner, KeyValueStore<Bytes, ByteArray>>(OwnerCommandsProcessorBinding.OWNER_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(ownerSerde)

        return ownersStream
                .groupByKey(Serialized.with(Serdes.String(), ownerSerde))
                .reduce({ _, y -> y }, ownerStateStore)
    }

    private fun createOwnerCommandResponseWindowedTable(commandResponseStream: KStream<String, CommandResponse>) {

        val commandResponseWindwedStateStore =
                Materialized.`as`<String, CommandResponse, WindowStore<Bytes, ByteArray>>(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(commandResponseSerde)
                        .withLoggingEnabled(emptyMap())
                        .withCachingEnabled()

        commandResponseStream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(60).toMillis()))
                .reduce({ _, y -> y }, commandResponseWindwedStateStore)
    }
}



