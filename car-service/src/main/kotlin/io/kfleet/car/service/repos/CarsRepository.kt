package io.kfleet.car.service.repos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.car.service.domain.Car
import io.kfleet.car.service.processor.CarStateCountProcessor
import io.kfleet.car.service.processor.CarStateCountProcessorBinding
import io.kfleet.car.service.rpclayer.RPC_CARS
import io.kfleet.common.WebClientUtil
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.InvalidStateStoreException
import org.apache.kafka.streams.state.QueryableStoreType
import org.apache.kafka.streams.state.StreamsMetadata
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.context.ApplicationContext
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono


@Component
class KafkaStreamsUtil(private val context: ApplicationContext) {
    fun getKafakStreams(): KafkaStreams {
        val beanNameCreatedBySpring = "&stream-builder-${CarStateCountProcessor::carStateUpdates.name}"
        val streamsBuilderFactoryBean = context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class.java)
        return streamsBuilderFactoryBean.kafkaStreams!!
    }
}

@Repository
class CarsRepository(
        private val interactiveQueryService: InteractiveQueryService,
        private val mapper: ObjectMapper,
        private val kafkaStreamsUtil: KafkaStreamsUtil,
        private val webclientUtil: WebClientUtil
) {

    fun findAllCars(): Flux<Car> {
        return getStreamMetaData()
                .map {
                    webclientUtil.doGetFlux(it.hostInfo(), "$RPC_CARS/", Car::class.java)
                }
                .flatMap { Flux.concat(it) }
    }


    fun findById(id: String): Mono<Car> {
        val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())
        return webclientUtil.doGet(hostInfo, "$RPC_CARS/$id", Car::class.java)
    }


    fun getCarsStateCounts(): Mono<Map<String, Long>> {
        return getStreamMetaData()
                .map {
                    webclientUtil.doGet(it.hostInfo(), "$RPC_CARS/stats", String::class.java)
                            .flatMap { rawStats ->
                                mapper.readValue<Map<String, Long>>(rawStats).toMono()
                            }
                }
                .flatMap { Flux.concat(it) }
                .reduce(mapOf(), { all, input -> all.plus(input) })
    }


    fun getStreamMetaData(): Flux<StreamsMetadata> {
        return Flux.defer {
            kafkaStreamsUtil.getKafakStreams().allMetadataForStore(CarStateCountProcessorBinding.CAR_STORE).toList().toFlux()
        }
    }

    fun <T> waitForStoreTobeQueryable(storeName: String, queryableStoreType: QueryableStoreType<T>): T {
        while (true) {
            try {
                val store = kafkaStreamsUtil.getKafakStreams().store(storeName, queryableStoreType)
                return store
            } catch (ignored: InvalidStateStoreException) {
                println(ignored)
                // store not yet ready for querying
                Thread.sleep(100)
            }

        }
    }

}

