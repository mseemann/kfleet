package io.kfleet.cars.service.processors

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.OwnerProcessor
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.commands.CommandResponse
import io.kfleet.common.createSerdeWithAvroRegistry
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.WindowStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import java.time.Duration


private val log = KotlinLogging.logger {}

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
class OwnerCommandsProcessor(
        @Value("\${spring.cloud.stream.schema-registry-client.endpoint}") private val endpoint: String,
        private val ownerProcessor: OwnerProcessor) {

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

        val createOwnerResult = createOwnerCommands
                .leftJoin(ownerTable) { command, owner ->
                    CommandAndOwner(command as CreateOwnerCommand, owner)
                }
                .flatMap { ownerId, commandAndOwner -> ownerProcessor.createOwner(ownerId, commandAndOwner) }

        createOwnerResult.foreach { k, v -> log.debug { "$k -> $v" } }

        createOwnerResult.filter { _, value -> value is Owner }.to(OwnerCommandsProcessorBinding.OWNSERS)
        createOwnerResult.filter { _, value -> value is OwnerCreatedEvent }.to(OwnerCommandsProcessorBinding.OWNER_EVENTS)
        createOwnerResult.filter { _, value -> value is CommandResponse }.to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)
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



