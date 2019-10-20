package io.kfleet.traveler.service.processors

import io.kfleet.commands.CommandResponse
import io.kfleet.common.createSerdeWithAvroRegistry
import io.kfleet.domain.events.traveler.TravelerCreatedEvent
import io.kfleet.domain.events.traveler.TravelerDeletedEvent
import io.kfleet.traveler.service.configuration.*
import io.kfleet.traveler.service.domain.Traveler
import io.kfleet.traveler.service.domain.TravelerProcessor
import io.kfleet.traveler.service.domain.isTravelerCommand
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.kafka.config.StreamsBuilderFactoryBean


private val log = KotlinLogging.logger {}

data class CommandAndTraveler(val command: SpecificRecord, val traveler: Traveler?)

interface TravelerCommandsProcessorBinding {

    @Input(TRAVELER_COMMANDS_IN)
    fun inputTravelerCommands(): KStream<String, SpecificRecord>

}


@EnableBinding(TravelerCommandsProcessorBinding::class)
class TravelerCommandsProcessor(
        private val context: ConfigurableApplicationContext,
        @Value("\${spring.cloud.stream.schema-registry-client.endpoint}") private val endpoint: String,
        private val travelerProcessor: TravelerProcessor) {

    private val travelerSerde by lazy { createSerdeWithAvroRegistry<Traveler>(endpoint)() }

    private val commandResponseSerde by lazy { createSerdeWithAvroRegistry<CommandResponse>(endpoint)() }

    private val travelerStore = Stores
            .keyValueStoreBuilder(Stores.persistentKeyValueStore(TRAVELER_RW_STORE),
                    Serdes.StringSerde(),
                    travelerSerde)

    private val commandResponseStore = Stores
            .keyValueStoreBuilder(Stores.persistentKeyValueStore(TRAVELER_COMMANDS_RESPONSE_STORE),
                    Serdes.StringSerde(),
                    commandResponseSerde)

    private val streamsBuilder: StreamsBuilder
        get() {
            val beanNameCreatedBySpring = "&stream-builder-${TravelerCommandsProcessor::processCommands.name}"
            return (context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class) as StreamsBuilderFactoryBean)
                    .getObject()
        }

    @StreamListener
    fun processCommands(@Input(TRAVELER_COMMANDS_IN) commandStream: KStream<String, SpecificRecord>) {

        streamsBuilder.addStateStore(travelerStore)
        streamsBuilder.addStateStore(commandResponseStore)

        val (travelerCommands, unknownCommands) = commandStream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value.isTravelerCommand() },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownCommands.to(DLQ)

        val processorResult = travelerCommands
                .transform(mapToTravelerAndCommand, TRAVELER_RW_STORE)
                .flatMap { _, commandAndTraveler -> travelerProcessor.processCommand(commandAndTraveler) }

        processorResult.foreach { k, v -> log.debug { "$k -> $v" } }

        processorResult
                .filter { _, value -> value is Traveler || value == null }
                .mapValues { v -> v as Traveler? }
                .process(writeTravelerToState, TRAVELER_RW_STORE)

        processorResult
                .filter { _, value ->
                    value is TravelerCreatedEvent
                            || value is TravelerDeletedEvent
                }
                .to(TRAVELER_EVENTS)


        processorResult
                .filter { _, value -> value is CommandResponse }
                .mapValues { v -> v as CommandResponse }
                .process(writeCommandResponseToState, TRAVELER_COMMANDS_RESPONSE_STORE)
    }

    private val writeTravelerToState = ProcessorSupplier {
        object : AbstractProcessor<String, Traveler?>() {

            lateinit var travelerStore: KeyValueStore<String, Traveler>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                travelerStore = context
                        .getStateStore(TRAVELER_RW_STORE) as KeyValueStore<String, Traveler>
            }

            override fun process(key: String, value: Traveler?) {
                log.debug { "put $value for $key" }
                travelerStore.put(key, value)
            }
        }
    }


    private val writeCommandResponseToState = ProcessorSupplier {
        object : AbstractProcessor<String, CommandResponse>() {

            lateinit var commandResponseStore: KeyValueStore<String, CommandResponse>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                commandResponseStore = context
                        .getStateStore(TRAVELER_COMMANDS_RESPONSE_STORE) as KeyValueStore<String, CommandResponse>
            }

            override fun process(key: String, value: CommandResponse) {
                log.debug { "put $value for $key" }
                commandResponseStore.put(key, value)
            }

        }
    }

    private val mapToTravelerAndCommand = TransformerSupplier {
        object : Transformer<String, SpecificRecord, KeyValue<String, CommandAndTraveler>> {
            lateinit var travelerStore: KeyValueStore<String, Traveler>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                travelerStore = context
                        .getStateStore(TRAVELER_RW_STORE) as KeyValueStore<String, Traveler>
            }

            override fun transform(travelerId: String, value: SpecificRecord): KeyValue<String, CommandAndTraveler> {
                val existingTraveler = travelerStore.get(travelerId)
                log.debug { "existing traveler. $existingTraveler" }
                return KeyValue(travelerId, CommandAndTraveler(value, existingTraveler))
            }

            override fun close() {}
        }
    }
}



