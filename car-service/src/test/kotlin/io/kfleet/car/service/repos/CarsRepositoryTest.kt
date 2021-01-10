package io.kfleet.owner.service.repos

import com.fasterxml.jackson.databind.ObjectMapper
import io.kfleet.car.service.domain.Car
import io.kfleet.car.service.domain.CarFactory
import io.kfleet.car.service.processor.CarStateCountProcessorBinding
import io.kfleet.car.service.repos.CarsRepository
import io.kfleet.car.service.repos.KafkaStreamsUtil
import io.kfleet.common.WebClientUtil
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.state.HostInfo
import org.apache.kafka.streams.state.StreamsMetadata
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.*
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class CarsRepositoryTest {

    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var kafkaStreamsUtil: KafkaStreamsUtil

    @Spy
    val objectMapper = ObjectMapper()

    @Mock
    lateinit var kafakStreams: KafkaStreams

    @Mock
    lateinit var webclientUtil: WebClientUtil

    @InjectMocks
    lateinit var carRepo: CarsRepository

    val car = CarFactory.createRandom(1)

    val hostInfo = HostInfo("localhost", 8084)

    val streamsMetadata = StreamsMetadata(hostInfo, emptySet(), emptySet())

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun findAllCars() {

        BDDMockito
            .given(kafakStreams.allMetadataForStore(CarStateCountProcessorBinding.CAR_STORE))
            .willReturn(listOf(streamsMetadata))
        BDDMockito
            .given(kafkaStreamsUtil.getKafkaStreams())
            .willReturn(kafakStreams)
        BDDMockito
            .given(webclientUtil.doGetFlux(hostInfo, "/rpc/cars/", Car::class.java))
            .willReturn(Flux.just(car))

        StepVerifier.create(carRepo.findAllCars())
            .expectNext(car)
            .verifyComplete()
    }

    @Test
    fun findById() {

        BDDMockito
            .given(
                interactiveQService.getHostInfo(
                    BDDMockito.anyString(),
                    BDDMockito.anyString(),
                    BDDMockito.any<StringSerializer>()
                )
            )
            .willReturn(hostInfo)

        BDDMockito
            .given(webclientUtil.doGet(hostInfo, "/rpc/cars/1", Car::class.java))
            .willReturn(Mono.just(car))

        StepVerifier.create(carRepo.findById(car.getId()))
            .expectNext(car)
            .verifyComplete()
    }

    @Test
    fun getCarsStateCounts() {
        BDDMockito
            .given(kafakStreams.allMetadataForStore(CarStateCountProcessorBinding.CAR_STORE))
            .willReturn(listOf(streamsMetadata))

        BDDMockito
            .given(kafkaStreamsUtil.getKafkaStreams())
            .willReturn(kafakStreams)

        BDDMockito
            .given(webclientUtil.doGet(hostInfo, "/rpc/cars/stats", String::class.java))
            .willReturn(Mono.just("{\"FREE\":1}"))

        StepVerifier.create(carRepo.getCarsStateCounts())
            .expectNext(mapOf("FREE" to 1L))
            .verifyComplete()
    }
}
