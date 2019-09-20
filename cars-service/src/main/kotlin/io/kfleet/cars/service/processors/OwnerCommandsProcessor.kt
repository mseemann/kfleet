package io.kfleet.cars.service.processors

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.OwnerCommand
import io.kfleet.cars.service.domain.Owner
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener


private val logger = KotlinLogging.logger {}


interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_EVENTS = "owner_events"
        const val OWNSERS = "owners"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, OwnerCommand>

    @Input(OWNSERS)
    fun inpuOwners(): KStream<String, Owner>
}


@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor {

    val serdeConfig = mapOf(
            AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to "http://localhost:8081")
    final val ownerSerde = SpecificAvroSerde<Owner>()

    init {
        ownerSerde.configure(serdeConfig, false)
    }


    @StreamListener()
    fun processCommands(
            @Input(OwnerCommandsProcessorBinding.OWNSERS) ownersStream: KStream<String, Owner>,
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS) commandStream: KStream<String, OwnerCommand>) {

        val ownerTable: KTable<String, Owner> = createOwnerKTable(ownersStream)

        val stream = commandStream
                .peek { key, value -> println("command: $key -> $value -> ${value.javaClass}") }

        val (createOwnerCommands, unknownOwnerCommands) = stream
                .branch(
                        Predicate<String, OwnerCommand> { key, value -> value.getCommand() is CreateOwnerCommand },
                        Predicate<String, OwnerCommand> { key, value -> true }
                )

        val (createOwnerStream, createOwnerStreamError) = createOwnerCommands
                //.selectKey { k, v -> v.getId() } not necessary - because the command id is the owner id - TODO needs to be changed
                .leftJoin(
                        ownerTable,
                        { event, owner ->
                            println("event: $event owner: $owner")
                            Pair(event, owner)
                        },
                        Joined
                                .keySerde<String, OwnerCommand, Owner>(Serdes.String())
                                .withValueSerde(SpecificAvroSerde()))
                .peek { key, value ->
                    println("joined: key: $key ->  event: ${value.first} owner: ${value.second}")
                }
                .branch(
                        Predicate<String, Pair<OwnerCommand, Owner?>> { _, value -> value.second == null },
                        Predicate<String, Pair<OwnerCommand, Owner?>> { _, value -> value.second !== null }
                )

        createOwnerStream
                .peek { key, value -> println("create a new owner") }
                .map { key, value -> KeyValue(key, Owner(key, (value.first.getCommand() as CreateOwnerCommand).getName(), listOf())) }
                .to(OwnerCommandsProcessorBinding.OWNSERS, Produced.with(Serdes.String(), ownerSerde))

        createOwnerStream
                .peek { key, value -> println("create a new owner created event, and create a positive command response") }

        createOwnerStreamError.peek { key, value -> println("reject create owner and create a negative command response") }

        unknownOwnerCommands.to("unknown_commands")


    }

    /**
     * In the Car-Domain this is done by Spring Boot Cloud materializedAs Feature
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



