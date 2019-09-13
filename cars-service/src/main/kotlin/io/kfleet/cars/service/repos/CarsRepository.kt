package io.kfleet.cars.service.repos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import io.kfleet.cars.service.events.Event
import io.kfleet.cars.service.processors.CarStateCountProcessor
import io.kfleet.cars.service.processors.CarStateCountProcessorBinding
import io.kfleet.common.headers
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.utils.Utils
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.context.ApplicationContext
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono


interface ICarsRepository {
    fun findAllCars(): Flux<Car>
    fun findById(id: String): Mono<Car>
    fun getCarsStateCounts(): Mono<Map<String, Long>>
}

interface CarsBinding {

    companion object {
        const val CAR_EVENTS = "car_events_out"
    }

    @Output(CAR_EVENTS)
    fun carEvents(): MessageChannel

}

@Repository
@EnableBinding(CarsBinding::class)
class CarsRepository(
        @Autowired val interactiveQueryService: InteractiveQueryService,
        @Autowired @Output(CarsBinding.CAR_EVENTS) val outputCarEvents: MessageChannel,
        @Autowired val mapper: ObjectMapper,
        @Autowired val context: ApplicationContext
) : ICarsRepository {


    private fun carsStore(): ReadOnlyKeyValueStore<String, String> = interactiveQueryService
            .getQueryableStore(CarStateCountProcessorBinding.CAR_STORE, QueryableStoreTypes.keyValueStore<String, String>())


    private fun carStateStore(): ReadOnlyKeyValueStore<String, Long> = interactiveQueryService
            .getQueryableStore(CarStateCountProcessorBinding.CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())

    private fun printHostForAllStates() {
        val beanNameCreatedBySpring = "&stream-builder-${CarStateCountProcessor::carStateUpdates.name}"
        val streamsBuilderFactoryBean = context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class.java)

        val kafkaStreams: KafkaStreams = streamsBuilderFactoryBean.kafkaStreams
        println(kafkaStreams.metrics())
        println(kafkaStreams.allMetadata())

        CarState.values().iterator().forEach {
            val metadata = kafkaStreams.metadataForKey(CarStateCountProcessorBinding.CAR_STATE_STORE, it.name, Serdes.String().serializer())
            println("${it.name} is stored in the app statestore on port: ${metadata.port()}")

            println("partition: ${Utils.toPositive(Utils.murmur2(it.name.toByteArray())) % 3}")
        }

    }


    override fun findAllCars(): Flux<Car> {
        return carsStore().all().use {
            it.asSequence().map { kv -> mapper.readValue<Car>(kv.value) }.toList().toFlux()
        }
    }

    override fun findById(id: String): Mono<Car> {
        val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())

        if (hostInfo == interactiveQueryService.currentHostInfo) {
            return carsStore().get(id)?.let {
                mapper.readValue<Car>(it).toMono()
            } ?: Mono.empty()
        }

        val webClient = WebClient.create("http://${hostInfo.host()}:${hostInfo.port()}")
        return webClient.get().uri("/cars/$id")
                .retrieve()
                .bodyToMono(String::class.java)
                .map { mapper.readValue<Car>(it) }
                .log()
    }


    override fun getCarsStateCounts(): Mono<Map<String, Long>> {
        printHostForAllStates()

        return carStateStore().all().use {
            it.asSequence().map { it.key to it.value }.toMap()
        }.toMono()
    }

    fun publishCarEvents(event: Event) {
        val msg = MessageBuilder.createMessage(event, headers(event.id))
        outputCarEvents.send(msg)
    }
}

