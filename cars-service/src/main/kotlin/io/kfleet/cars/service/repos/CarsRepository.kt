package io.kfleet.cars.service.repos

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.processors.CarStateCountProcessor
import io.kfleet.cars.service.processors.CarStateCountProcessorBinding
import io.kfleet.cars.service.rpclayer.CARS_RPC
import mu.KotlinLogging
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.context.ApplicationContext
import org.springframework.kafka.config.StreamsBuilderFactoryBean
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

private val log = KotlinLogging.logger {}


interface ICarsRepository {
    fun findAllCars(): Flux<Car>
    fun findById(id: String): Mono<Car>
    fun getCarsStateCounts(): Mono<Map<String, Long>>
}


@Repository
class CarsRepository(
        @Autowired val interactiveQueryService: InteractiveQueryService,
        @Autowired val mapper: ObjectMapper,
        @Autowired val context: ApplicationContext
) : ICarsRepository {

    override fun findAllCars(): Flux<Car> {
        return getStreamMetaData()
                .map {
                    val webClient = WebClient.create("http://${it.host()}:${it.port()}")
                    webClient.get().uri("/$CARS_RPC/").retrieve().bodyToFlux(Car::class.java)
                }
                .flatMap { Flux.concat(it) }
    }


    override fun findById(id: String): Mono<Car> {
        val hostInfo = interactiveQueryService.getHostInfo(CarStateCountProcessorBinding.CAR_STORE, id, StringSerializer())
        val webClient = WebClient.create("http://${hostInfo.host()}:${hostInfo.port()}")
        return webClient.get().uri("/$CARS_RPC/$id").retrieve().bodyToMono(Car::class.java)
    }


    override fun getCarsStateCounts(): Mono<Map<String, Long>> {
        return getStreamMetaData()
                .map {
                    val webClient = WebClient.create("http://${it.host()}:${it.port()}")
                    webClient.get().uri("/$CARS_RPC/stats")
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

