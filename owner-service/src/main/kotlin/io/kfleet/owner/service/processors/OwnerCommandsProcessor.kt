package io.kfleet.owner.service.processors

import io.kfleet.commands.CommandResponse
import io.kfleet.common.createSerdeWithAvroRegistry
import io.kfleet.domain.events.car.CarDeregisteredEvent
import io.kfleet.domain.events.car.CarRegisteredEvent
import io.kfleet.domain.events.owner.OwnerCreatedEvent
import io.kfleet.domain.events.owner.OwnerDeletedEvent
import io.kfleet.domain.events.owner.OwnerUpdatedEvent
import io.kfleet.owner.service.configuration.StoreNames
import io.kfleet.owner.service.configuration.TopicBindingNames
import io.kfleet.owner.service.configuration.TopicNames
import io.kfleet.owner.service.domain.Owner
import io.kfleet.owner.service.domain.OwnerProcessor
import io.kfleet.owner.service.domain.isOwnerCommand
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Transformer
import org.apache.kafka.streams.kstream.TransformerSupplier
import org.apache.kafka.streams.processor.AbstractProcessor
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

    @Input(TopicBindingNames.OWNER_COMMANDS_IN)
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
            .keyValueStoreBuilder(Stores.persistentKeyValueStore(StoreNames.OWNER_RW_STORE),
                    Serdes.StringSerde(),
                    ownerSerde)

    private val commandResponseWindowedStore = Stores
            .windowStoreBuilder(Stores.persistentWindowStore(
                    StoreNames.OWNER_COMMANDS_RESPONSE_STORE,
                    Duration.ofDays(1).toMillis(),
                    3,
                    Duration.ofHours(1).toMillis(),
                    false),
                    Serdes.StringSerde(),
                    commandResponseSerde)

    private val streamsBuilder: StreamsBuilder
        get() {
            val beanNameCreatedBySpring = "&stream-builder-${OwnerCommandsProcessor::processCommands.name}"
            return (context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class) as StreamsBuilderFactoryBean)
                    .getObject()
        }

    @StreamListener
    fun processCommands(@Input(TopicBindingNames.OWNER_COMMANDS_IN) commandStream: KStream<String, SpecificRecord>) {

        streamsBuilder.addStateStore(ownerStateStore)
        streamsBuilder.addStateStore(commandResponseWindowedStore)

        val (ownerCommands, unknownCommands) = commandStream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value.isOwnerCommand() },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownCommands.to(TopicNames.DLQ)

        val createOwnerResult = ownerCommands
                .transform(mapToOwnerAndCommand, StoreNames.OWNER_RW_STORE)
                .flatMap { _, commandAndOwner -> ownerProcessor.processCommand(commandAndOwner) }

        createOwnerResult.foreach { k, v -> log.debug { "$k -> $v" } }

        createOwnerResult
                .filter { _, value -> value is Owner || value == null }
                .mapValues { v -> v as Owner? }
                .process(writeOwnerToState, StoreNames.OWNER_RW_STORE)

        createOwnerResult
                .filter { _, value ->
                    value is OwnerCreatedEvent
                            || value is OwnerUpdatedEvent
                            || value is OwnerDeletedEvent
                }
                .to(TopicNames.OWNER_EVENTS)

        createOwnerResult
                .filter { _, value ->
                    value is CarRegisteredEvent
                            || value is CarDeregisteredEvent
                }
                .to(TopicNames.CAR_EVENTS)

        createOwnerResult
                .filter { _, value -> value is CommandResponse }
                .mapValues { v -> v as CommandResponse }
                .process(writeCommandResponseToState, StoreNames.OWNER_COMMANDS_RESPONSE_STORE)
    }

    private val writeOwnerToState = ProcessorSupplier {
        object : AbstractProcessor<String, Owner?>() {

            lateinit var ownerStore: KeyValueStore<String, Owner>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                ownerStore = context
                        .getStateStore(StoreNames.OWNER_RW_STORE) as KeyValueStore<String, Owner>
            }

            override fun process(key: String, value: Owner?) {
                log.debug { "put $value for $key" }
                ownerStore.put(key, value)
            }
        }
    }


    private val writeCommandResponseToState = ProcessorSupplier {
        object : AbstractProcessor<String, CommandResponse>() {

            lateinit var commandResponseStore: WindowStore<String, CommandResponse>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                commandResponseStore = context
                        .getStateStore(StoreNames.OWNER_COMMANDS_RESPONSE_STORE) as WindowStore<String, CommandResponse>
            }

            override fun process(key: String, value: CommandResponse) {
                log.debug { "put $value for $key" }
                commandResponseStore.put(key, value)
            }

        }
    }

    private val mapToOwnerAndCommand = TransformerSupplier {
        object : Transformer<String, SpecificRecord, KeyValue<String, CommandAndOwner>> {
            lateinit var ownerStore: KeyValueStore<String, Owner>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                ownerStore = context
                        .getStateStore(StoreNames.OWNER_RW_STORE) as KeyValueStore<String, Owner>
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



