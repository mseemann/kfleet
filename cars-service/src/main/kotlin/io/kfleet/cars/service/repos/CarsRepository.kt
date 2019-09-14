package io.kfleet.cars.service.repos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.events.Event
import io.kfleet.cars.service.processors.CarStateCountProcessor
import io.kfleet.cars.service.processors.CarStateCountProcessorBinding
import io.kfleet.common.headers
import mu.KotlinLogging
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.apache.kafka.streams.state.StreamsMetadata
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
import reactor.core.publisher.*

private val logger = KotlinLogging.logger {}

private const val CARS_LOCAL_ONLY = "cars-local-only"

interface ICarsRepository {
    fun findAllCars(): Flux<Car>
    fun findById(id: String): Mono<Car>
    fun getCarsStateCounts(): Mono<Map<String, Long>>
    fun findByIdLocal(id: String): Mono<Car>
    fun findAllCarsLocal(): Flux<Car>
    fun getLocalCarsStateCounts(): Mono<Map<String, Long>>
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

    private fun getKafakStreams(): KafkaStreams {
        val beanNameCreatedBySpring = "&stream-builder-${CarStateCountProcessor::carStateUpdates.name}"
        val streamsBuilderFactoryBean = context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class.java)

        logger.debug { "kafak streams wird neu gelesen" }
        return streamsBuilderFactoryBean.kafkaStreams!!
    }

    override fun findAllCars(): Flux<Car> {

        return Flux.create { sink: FluxSink<Array<StreamsMetadata>> ->
            val streamMetadata = getKafakStreams().allMetadataForStore(CarStateCountProcessorBinding.CAR_STORE)
            sink.next(streamMetadata.toTypedArray())
            sink.complete()
        }.map { arrayOfStreamsMetadata: Array<StreamsMetadata> ->
            arrayOfStreamsMetadata.map {
                if (it.hostInfo() == interactiveQueryService.currentHostInfo) {
                    logger.debug { "find cars local ${it.port()}" }
                    findAllCarsLocal()
                } else {
                    logger.debug { "find cars remote per rpc ${it.port()}" }
                    val webClient = WebClient.create("http://${it.host()}:${it.port()}")
                    webClient.get().uri("/$CARS_LOCAL_ONLY/")
                            .retrieve()
                            .bodyToFlux(String::class.java)
                            .flatMap { rawCars -> mapper.readValue<List<Car>>(rawCars).toList().toFlux() }
                }
            }
        }.flatMap {
            Flux.create<Car> { sink ->
                val result = mutableListOf<Car>()
                Flux.concat(it).subscribe(
                        { result.add(it) },
                        { error -> sink.error(error) },
                        {
                            result.forEach { sink.next(it) }
                            sink.complete()
                        })
            }
        }
    }

    override fun findAllCarsLocal(): Flux<Car> {
        return carsStore().all().use {
            it.asSequence().map { kv -> mapper.readValue<Car>(kv.value) }.toList().toFlux()
        }
    }

    override fun findById(id: String): Mono<Car> {
        val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())

        if (hostInfo == interactiveQueryService.currentHostInfo) {
            return findByIdLocal(id)
        }

        val webClient = WebClient.create("http://${hostInfo.host()}:${hostInfo.port()}")
        return webClient.get().uri("/$CARS_LOCAL_ONLY/$id")
                .retrieve()
                .bodyToMono(String::class.java)
                .map { mapper.readValue<Car>(it) }
                .log()
    }

    override fun findByIdLocal(id: String): Mono<Car> {
        return carsStore().get(id)?.let {
            mapper.readValue<Car>(it).toMono()
        } ?: Mono.empty()
    }

    override fun getCarsStateCounts(): Mono<Map<String, Long>> {
        return Mono.create { sink: MonoSink<Array<StreamsMetadata>> ->
            val streamMetadata = getKafakStreams().allMetadataForStore(CarStateCountProcessorBinding.CAR_STORE)
            sink.success(streamMetadata.toTypedArray())
        }.map { arrayOfStreamsMetadata: Array<StreamsMetadata> ->
            arrayOfStreamsMetadata.map {
                if (it.hostInfo() == interactiveQueryService.currentHostInfo) {
                    logger.debug { "find cars stats local ${it.port()}" }
                    getLocalCarsStateCounts()
                } else {
                    logger.debug { "find cars stats remote per rpc ${it.port()}" }
                    val webClient = WebClient.create("http://${it.host()}:${it.port()}")
                    webClient.get().uri("/$CARS_LOCAL_ONLY/stats")
                            .retrieve()
                            .bodyToMono(String::class.java)
                            .flatMap { rawCars -> mapper.readValue<Map<String, Long>>(rawCars).toMono() }
                }
            }
        }.flatMap {
            Mono.create { sink: MonoSink<Map<String, Long>> ->
                val result = mutableMapOf<String, Long>()
                Flux.concat(it).subscribe(
                        { result.putAll(it) },
                        { error -> sink.error(error) },
                        { sink.success(result) })
            }
        }
    }

    override fun getLocalCarsStateCounts(): Mono<Map<String, Long>> {
        return carStateStore().all().use { allCars ->
            allCars.asSequence().map { it.key to it.value }.toMap()
        }.toMono()
    }

    fun publishCarEvents(event: Event) {
        val msg = MessageBuilder.createMessage(event, headers(event.id))
        outputCarEvents.send(msg)
    }
}

