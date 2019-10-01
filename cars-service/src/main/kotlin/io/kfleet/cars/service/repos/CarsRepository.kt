package io.kfleet.cars.service.repos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.processors.CarStateCountProcessor
import io.kfleet.cars.service.processors.CarStateCountProcessorBinding
import io.kfleet.cars.service.rpclayer.RPC_CARS
import io.kfleet.common.createWebClient
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.context.ApplicationContext
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono


@Repository
class CarsRepository(
        private val interactiveQueryService: InteractiveQueryService,
        private val mapper: ObjectMapper,
        private val context: ApplicationContext
) {

    fun findAllCars(): Flux<Car> {
        return getStreamMetaData()
                .map {
                    createWebClient(it.hostInfo()).get().uri("$RPC_CARS/").retrieve().bodyToFlux(Car::class.java)
                }
                .flatMap { Flux.concat(it) }
    }


    fun findById(id: String): Mono<Car> {
        val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())
        return createWebClient(hostInfo).get().uri("$RPC_CARS/$id").retrieve().bodyToMono(Car::class.java)
    }


    fun getCarsStateCounts(): Mono<Map<String, Long>> {
        return getStreamMetaData()
                .map {
                    createWebClient(it.hostInfo()).get().uri("$RPC_CARS/stats")
                            .retrieve()
                            .bodyToMono(String::class.java)
                            .flatMap { rawStats -> mapper.readValue<Map<String, Long>>(rawStats).toMono() }
                }
                .flatMap { Flux.concat(it) }
                .reduce(mapOf(), { all, input -> all.plus(input) })
    }


    private fun getKafakStreams(): KafkaStreams {
        val beanNameCreatedBySpring = "&stream-builder-${CarStateCountProcessor::carStateUpdates.name}"
        val streamsBuilderFactoryBean = context.getBean(beanNameCreatedBySpring, StreamsBuilderFactoryBean::class.java)
        return streamsBuilderFactoryBean.kafkaStreams!!
    }

    private fun getStreamMetaData() = Flux.defer {
        getKafakStreams().allMetadataForStore(CarStateCountProcessorBinding.CAR_STORE).toList().toFlux()
    }

}

