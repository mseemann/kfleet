package io.kfleet.car.service.processor

import io.kfleet.car.service.configuration.CARS
import io.kfleet.car.service.configuration.CAR_EVENTS_IN
import io.kfleet.car.service.configuration.CAR_RW_STORE
import io.kfleet.car.service.configuration.DLQ
import io.kfleet.car.service.domain.Car
import io.kfleet.car.service.domain.CarProcessor
import io.kfleet.common.createSerdeWithAvroRegistry
import io.kfleet.domain.events.isCarEvent
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

interface CarEventsProcessorBinding {

    @Input(CAR_EVENTS_IN)
    fun inputCarEvents(): KStream<String, SpecificRecord>

}

data class CarAndEvent(val car: Car?, val carEvent: SpecificRecord)

@EnableBinding(CarEventsProcessorBinding::class)
class CarEventProcessor(
        private val context: ConfigurableApplicationContext,
        @Value("\${spring.cloud.stream.schema-registry-client.endpoint}") private val endpoint: String,
        private val carProcessor: CarProcessor) {

    private val carSerde by lazy { createSerdeWithAvroRegistry<Car>(endpoint)() }

    private val carStateStore = Stores
            .keyValueStoreBuilder(Stores.persistentKeyValueStore(CAR_RW_STORE),
                    Serdes.StringSerde(),
                    carSerde)

    private val streamsBuilder: StreamsBuilder
        get() {
            val beanNameCreatedBySpring = "&stream-builder-${CarEventProcessor::processEvents.name}"
            return (context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class) as StreamsBuilderFactoryBean)
                    .getObject()
        }


    @StreamListener
    fun processEvents(@Input(CAR_EVENTS_IN) carEventStream: KStream<String, SpecificRecord>) {

        streamsBuilder.addStateStore(carStateStore)

        val (carEvents, unknownMessages) = carEventStream
                .branch(
                        Predicate<String, SpecificRecord> { _, value -> value.isCarEvent() },
                        Predicate<String, SpecificRecord> { _, _ -> true }
                )

        unknownMessages.to(DLQ)

        val carResults = carEvents
                .transform(mapToCarAndEvent, CAR_RW_STORE)
                .flatMap { _, eventAndCar -> carProcessor.processEvent(eventAndCar) }

        carResults.foreach { k, v -> log.debug { "$k -> $v" } }

        carResults
                .filter { _, value -> value is Car }
                .through(CARS)
                .mapValues { v -> v as Car }
                .process(writeCarToState, CAR_RW_STORE)

    }

    private val mapToCarAndEvent = TransformerSupplier {
        object : Transformer<String, SpecificRecord, KeyValue<String, CarAndEvent>> {
            lateinit var carStore: KeyValueStore<String, Car>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                carStore = context
                        .getStateStore(CAR_RW_STORE) as KeyValueStore<String, Car>
            }

            override fun transform(ownerId: String, value: SpecificRecord): KeyValue<String, CarAndEvent> {
                val existingCar = carStore.get(ownerId)
                log.debug { "existing car. $existingCar" }
                return KeyValue(ownerId, CarAndEvent(existingCar, value))
            }

            override fun close() {}
        }
    }

    private val writeCarToState = ProcessorSupplier {
        object : AbstractProcessor<String, Car>() {

            lateinit var carStore: KeyValueStore<String, Car>

            override fun init(context: ProcessorContext) {
                @Suppress("UNCHECKED_CAST")
                carStore = context
                        .getStateStore(CAR_RW_STORE) as KeyValueStore<String, Car>
            }

            override fun process(key: String, value: Car) {
                log.debug { "put $value for $key" }
                carStore.put(key, value)
            }
        }
    }

}
