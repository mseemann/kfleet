package io.kfleet.cars.service.processors

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import mu.KotlinLogging
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

private val log = KotlinLogging.logger {}

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

    private val ownerSerde: SpecificAvroSerde<Owner> by lazy {

        val serdeConfig = mapOf(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to endpoint)

        val serde = SpecificAvroSerde<Owner>()
        serde.configure(serdeConfig, false)
        serde
    }

    private val commandResponseSerde: SpecificAvroSerde<CommandResponse> by lazy {

        val serdeConfig = mapOf(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to endpoint)

        val serde = SpecificAvroSerde<CommandResponse>()
        serde.configure(serdeConfig, false)
        serde
    }

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

        val (createOwnerStream, ownerNotCreatedStream) = createOwnerCommands
                .leftJoin(ownerTable) { event, owner -> Pair(event, owner) }
                .branch(
                        Predicate<String, Pair<SpecificRecord, Owner?>> { _, value -> value.second == null },
                        Predicate<String, Pair<SpecificRecord, Owner?>> { _, value -> value.second !== null }
                )

        createOwnerStream
                .peek { _, _ -> log.debug { "create a new owner" } }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    val owner = Owner.newBuilder().apply {
                        id = key
                        name = event.getName()
                    }.build()
                    KeyValue(key, owner)
                }
                .to(OwnerCommandsProcessorBinding.OWNSERS)

        createOwnerStream
                .peek { _, _ -> log.debug { "owner created event created" } }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    val ownerCreatedEvents = OwnerCreatedEvent.newBuilder().apply {
                        ownerId = event.getOwnerId()
                        name = event.getName()
                    }.build()
                    KeyValue(key, ownerCreatedEvents)
                }
                .to(OwnerCommandsProcessorBinding.OWNER_EVENTS)

        createOwnerStream
                .peek { _, _ -> log.debug { "owner created succees command created" } }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    val response = CommandResponse.newBuilder().apply {
                        commandId = event.getCommandId()
                        status = CommandStatus.SUCCEEDED
                    }.build()
                    KeyValue(event.getCommandId(), response)
                }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)

        ownerNotCreatedStream
                .peek { _, _ -> log.debug { "owner created reject command created" } }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    val response = CommandResponse.newBuilder().apply {
                        commandId = event.getCommandId()
                        status = CommandStatus.REJECTED
                        reason = "Owner with id $key already exists"
                    }.build()
                    KeyValue(event.getCommandId(), response)
                }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)

    }

    /**
     * In the Car-Domain this is done with the Spring Boot Cloud Streams materializedAs Feature
     */
    private fun createOwnerKTable(ownersStream: KStream<String, Owner>): KTable<String, Owner> {

        val ownerStateStore: Materialized<String, Owner, KeyValueStore<Bytes, ByteArray>> =
                Materialized.`as`<String, Owner, KeyValueStore<Bytes, ByteArray>>(OwnerCommandsProcessorBinding.OWNER_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(ownerSerde)

        val ownerTable: KTable<String, Owner> = ownersStream
                .peek { key, value ->
                    log.debug { "process owner from ownerstream: $key -> $value -> ${value.javaClass}" }
                }
                .groupByKey(Serialized.with(Serdes.String(), ownerSerde))
                .reduce({ _, y -> y }, ownerStateStore)

        ownerTable.toStream().peek { key, value ->
            log.debug { "owner-table steam: $key -> $value -> ${value.javaClass}" }
        }

        return ownerTable
    }

    private fun createOwnerCommandResponseWindowedTable(commandResponseStream: KStream<String, CommandResponse>) {

        val commandResponseWindwedStateStore: Materialized<String, CommandResponse, WindowStore<Bytes, ByteArray>> =
                Materialized.`as`<String, CommandResponse, WindowStore<Bytes, ByteArray>>(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(commandResponseSerde)

        val responseTable: KTable<Windowed<String>, CommandResponse> = commandResponseStream
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(60).toMillis()))
                .reduce({ _, y -> y }, commandResponseWindwedStateStore)

        responseTable.toStream().peek { key, value ->
            log.debug { "responseTable steam: $key -> $value -> ${value.javaClass}" }
        }
    }
}



