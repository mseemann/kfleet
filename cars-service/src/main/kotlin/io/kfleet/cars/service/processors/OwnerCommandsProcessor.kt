package io.kfleet.cars.service.processors

import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.OwnerProcessor
import io.kfleet.cars.service.domain.isOwnerCommand
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.commands.CommandResponse
import io.kfleet.common.createSerdeWithAvroRegistry
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.kstream.TransformerSupplier
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.ProcessorSupplier
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

data class CommandAndOwner(val command: SpecificRecord, val owner: Owner?)

interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_EVENTS = "owner_events"
        const val UNKNOW_COMMANDS = "unknown_owner_commands"
        // backed by a topic cars-service-owners-owner_commands_response_store-changelog
        const val OWNER_COMMANDS_RESPONSE_STORE = "owner_commands_response_store"
        // backed by a topic cars-service-owners-owners-changelog
        const val OWNER_RW_STORE = "owners_store"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, SpecificRecord>

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
                    Serdes.StringSerde(),
                    ownerSerde)

    private val commandResponseWindowedStore = Stores
            .windowStoreBuilder(Stores.persistentWindowStore(
                    OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE,
                    Duration.ofDays(1).toMillis(),
                    3,
                    Duration.ofHours(1).toMillis(),
                    false),
                    Serdes.StringSerde(),
                    commandResponseSerde)

    @StreamListener
    fun processCommands(@Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS) commandStream: KStream<String, SpecificRecord>) {

        val beanNameCreatedBySpring = "&stream-builder-${OwnerCommandsProcessor::processCommands.name}"
        val streamsBuilderFactory = (context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class) as StreamsBuilderFactoryBean)
                .getObject()

        streamsBuilderFactory.addStateStore(ownerStateStore)
        streamsBuilderFactory.addStateStore(commandResponseWindowedStore)

        val (ownerCommands, unknownCommands) = commandStream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value.isOwnerCommand() },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownCommands.to(OwnerCommandsProcessorBinding.UNKNOW_COMMANDS)

        val createOwnerResult = ownerCommands
                .transform(mapToOwnerAndCommand, OwnerCommandsProcessorBinding.OWNER_RW_STORE)
                .flatMap { _, commandAndOwner -> ownerProcessor.processCommand(commandAndOwner) }

        createOwnerResult.foreach { k, v -> log.debug { "$k -> $v" } }

        createOwnerResult
                .filter { _, value -> value is Owner || value == null }
                .mapValues { v -> v as Owner }
                .process(writeOwnerToState, OwnerCommandsProcessorBinding.OWNER_RW_STORE)

        createOwnerResult
                .filter { _, value -> value is OwnerCreatedEvent }
                .to(OwnerCommandsProcessorBinding.OWNER_EVENTS)

        createOwnerResult
                .filter { _, value -> value is CommandResponse }
                .mapValues { v -> v as CommandResponse }
                .process(writeCommandResponseToState, OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE)
    }

    private val writeOwnerToState = ProcessorSupplier {
        object : Processor<String, Owner> {

            lateinit var ownerStore: KeyValueStore<String, Owner>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                ownerStore = context
                        .getStateStore(OwnerCommandsProcessorBinding.OWNER_RW_STORE) as KeyValueStore<String, Owner>
            }

            override fun process(key: String, value: Owner?) {
                log.debug { "put $value for $key" }
                ownerStore.put(key, value)
            }

            override fun close() {}

        }
    }


    private val writeCommandResponseToState = ProcessorSupplier {
        object : Processor<String, CommandResponse> {

            lateinit var commandResponseStore: WindowStore<String, CommandResponse>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                commandResponseStore = context
                        .getStateStore(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE) as WindowStore<String, CommandResponse>
            }

            override fun process(key: String, value: CommandResponse) {
                log.debug { "put $value for $key" }
                commandResponseStore.put(key, value)
            }

            override fun close() {}

        }
    }

    private val mapToOwnerAndCommand = TransformerSupplier {
        object : Transformer<String, SpecificRecord, KeyValue<String, CommandAndOwner>> {
            lateinit var ownerStore: KeyValueStore<String, Owner>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                ownerStore = context
                        .getStateStore(OwnerCommandsProcessorBinding.OWNER_RW_STORE) as KeyValueStore<String, Owner>
            }

            override fun transform(ownerId: String, value: SpecificRecord): KeyValue<String, CommandAndOwner> {
                val existingOwner = ownerStore.get(ownerId)
                log.debug { "existing owner. $existingOwner" }
                return KeyValue(ownerId, CommandAndOwner(value, existingOwner))
            }

            override fun close() {}
        }
    }
}



