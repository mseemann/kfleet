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
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.Stores
import org.apache.kafka.streams.state.WindowStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import java.time.Duration


private val log = KotlinLogging.logger {}

data class CommandAndOwner(val command: CreateOwnerCommand, val owner: Owner?)

interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_COMMANDS_RESPONSE = "owner_commands_response"
        const val OWNER_EVENTS = "owner_events"
        const val UNKNOW_COMMANDS = "unknown_owner_commands"
        const val OWNER_COMMANDS_RESPONSE_STORE = "owner_commands_response_store"
        // backed by a topic cars-service-owners-owners-changelog
        const val OWNER_RW_STORE = "owners"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, SpecificRecord>

    @Input(OWNER_COMMANDS_RESPONSE)
    fun inputOwnerCommandResponses(): KStream<String, CommandResponse>
}


@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor(
        private val context: ConfigurableApplicationContext,
        @Value("\${spring.cloud.stream.schema-registry-client.endpoint}") private val endpoint: String,
        private val ownerProcessor: OwnerProcessor) {

    private val ownerSerde by lazy { createSerdeWithAvroRegistry<Owner>(endpoint)() }

    private val commandResponseSerde by lazy { createSerdeWithAvroRegistry<CommandResponse>(endpoint)() }

    private val ownerStateStore = Stores
            .keyValueStoreBuilder(Stores.persistentKeyValueStore(OwnerCommandsProcessorBinding.OWNER_RW_STORE),
                    Serdes.StringSerde(), ownerSerde)
            .withLoggingEnabled(mapOf())

    @StreamListener
    fun processCommands(
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS) commandStream: KStream<String, SpecificRecord>,
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE) commandResponseStream: KStream<String, CommandResponse>) {

        createOwnerCommandResponseWindowedTable(commandResponseStream)

        val beanNameCreatedBySpring = "&stream-builder-${OwnerCommandsProcessor::processCommands.name}"
        (context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class) as StreamsBuilderFactoryBean)
                .getObject()
                .addStateStore(ownerStateStore);

        val (createOwnerCommands, unknownOwnerCommands) = commandStream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value is CreateOwnerCommand },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownOwnerCommands.to(OwnerCommandsProcessorBinding.UNKNOW_COMMANDS)

        val createOwnerResult = createOwnerCommands.transform(TransformerSupplier {
            object : Transformer<String, SpecificRecord, KeyValue<String, CommandAndOwner>> {
                lateinit var ownerStore: KeyValueStore<String, Owner>

                override fun init(context: ProcessorContext) {
                    val store = context.getStateStore(OwnerCommandsProcessorBinding.OWNER_RW_STORE)
                    @Suppress("UNCHECKED_CAST")
                    ownerStore = store as KeyValueStore<String, Owner>
                }

                override fun transform(key: String, value: SpecificRecord): KeyValue<String, CommandAndOwner> {
                    val command = value as CreateOwnerCommand
                    val existingOwner = ownerStore.get(command.getOwnerId())
                    log.debug { "exisiting owner. $existingOwner" }
                    return KeyValue(key, CommandAndOwner(command, existingOwner))
                }

                override fun close() {}
            }
        }, OwnerCommandsProcessorBinding.OWNER_RW_STORE)
                .flatMap { ownerId, commandAndOwner -> ownerProcessor.createOwner(ownerId, commandAndOwner) }

        createOwnerResult.foreach { k, v -> log.debug { "$k -> $v" } }

        createOwnerResult
                .filter { _, value -> value is Owner }
                .mapValues { v -> v as Owner }
                .transform(TransformerSupplier {
                    object : Transformer<String, Owner, KeyValue<String, Owner>> {
                        lateinit var ownerStore: KeyValueStore<String, Owner>

                        override fun init(context: ProcessorContext) {
                            val store = context.getStateStore(OwnerCommandsProcessorBinding.OWNER_RW_STORE)
                            @Suppress("UNCHECKED_CAST")
                            ownerStore = store as KeyValueStore<String, Owner>
                        }

                        override fun transform(key: String, value: Owner): KeyValue<String, Owner> {
                            log.debug { "put $value for $key" }
                            ownerStore.put(key, value)
                            return KeyValue(key, value)
                        }

                        override fun close() {}

                    }
                }, OwnerCommandsProcessorBinding.OWNER_RW_STORE)
        createOwnerResult.filter { _, value -> value is OwnerCreatedEvent }
                .to(OwnerCommandsProcessorBinding.OWNER_EVENTS)
        createOwnerResult.filter { _, value -> value is CommandResponse }
                .to(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE)
    }


    /**
     * In the Car-Domain this is done with the Spring Boot Cloud Streams materializedAs Feature
     */
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



