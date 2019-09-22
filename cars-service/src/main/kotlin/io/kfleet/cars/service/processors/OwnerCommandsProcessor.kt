package io.kfleet.cars.service.processors

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.commands.CommandRejected
import io.kfleet.commands.CommandSucceeded
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener


private val logger = KotlinLogging.logger {}


interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_COMMANDS_RESPONSE = "owner_commands_response"
        const val OWNER_EVENTS = "owner_events"
        const val OWNSERS = "owners"
        const val UNKNOW_COMMANDS = "unknown_owner_commands"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, SpecificRecord>

    @Input(OWNSERS)
    fun inpuOwners(): KStream<String, Owner>
}


@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor(@Value("\${spring.cloud.stream.schema-registry-client.endpoint}") val endpoint: String) {

    private val ownerSerde = SpecificAvroSerde<Owner>()

    init {
        val serdeConfig = mapOf(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to endpoint)

        ownerSerde.configure(serdeConfig, false)
    }


    @StreamListener
    fun processCommands(
            @Input(OwnerCommandsProcessorBinding.OWNSERS) ownersStream: KStream<String, Owner>,
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS) commandStream: KStream<String, SpecificRecord>) {

        val ownerTable: KTable<String, Owner> = createOwnerKTable(ownersStream)

        val stream = commandStream
                .peek { key, value -> println("command: $key -> $value -> ${value.javaClass}") }

        val (createOwnerCommands, unknownOwnerCommands) = stream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value is CreateOwnerCommand },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownOwnerCommands.to(OwnerCommandsProcessorBinding.UNKNOW_COMMANDS)

        val (createOwnerStream, ownerNotCreatedStream) = createOwnerCommands
                .leftJoin(ownerTable) { event, owner -> Pair(event, owner) }
                .peek { key, value ->
                    println("joined: key: $key ->  event: ${value.first} owner: ${value.second}")
                }
                .branch(
                        Predicate<String, Pair<SpecificRecord, Owner?>> { _, value -> value.second == null },
                        Predicate<String, Pair<SpecificRecord, Owner?>> { _, value -> value.second !== null }
                )

        createOwnerStream
                .peek { _, _ -> println("create a new owner") }
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
                .peek { _, _ -> println("create a new owner created event") }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    KeyValue(key, OwnerCreatedEvent(event.getOwnerId(), event.getName()))
                }
                .to(OwnerCommandsProcessorBinding.OWNER_EVENTS)

        createOwnerStream
                .peek { _, _ -> println("create a positive command response") }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    KeyValue(key, CommandSucceeded(event.getCommandId()))
                }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)

        ownerNotCreatedStream
                .peek { _, _ -> println("reject create owner: create a negative command response") }
                .map { key, value ->
                    val event = value.first as CreateOwnerCommand
                    KeyValue(key, CommandRejected(event.getCommandId(), "Owner with id $key already exists"))
                }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)

    }

    /**
     * In the Car-Domain this is done with the Spring Boot Cloud Streams materializedAs Feature
     */
    private fun createOwnerKTable(ownersStream: KStream<String, Owner>): KTable<String, Owner> {

        val ownerStateStore: Materialized<String, Owner, KeyValueStore<Bytes, ByteArray>> =
                Materialized.`as`<String, Owner, KeyValueStore<Bytes, ByteArray>>("owner-store")
                        .withKeySerde(Serdes.String())
                        .withValueSerde(ownerSerde)

        val ownerTable: KTable<String, Owner> = ownersStream
                .peek { key, value -> println("process owner from ownerstream: $key -> $value -> ${value.javaClass}") }
                .groupByKey(Serialized.with(Serdes.String(), ownerSerde))
                .reduce({ _, y -> y }, ownerStateStore)

        ownerTable.toStream().peek { key, value -> println("owner-table steam: $key -> $value -> ${value.javaClass}") }

        return ownerTable
    }
}



