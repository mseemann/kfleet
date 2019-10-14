package io.kfleet.owner.service.repos

import io.kfleet.car.service.domain.CarFactory
import io.kfleet.car.service.processor.CarStateCountProcessorBinding
import io.kfleet.car.service.repos.CarsLocalRepository
import io.kfleet.cars.service.domain.Car
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.state.KeyValueIterator
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.*
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import reactor.test.StepVerifier

class CarsLocalRepositoryTest {
    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var carStore: ReadOnlyKeyValueStore<String, Car>

    @Mock
    lateinit var carStateStore: ReadOnlyKeyValueStore<String, Long>

    @InjectMocks
    lateinit var carRepo: CarsLocalRepository

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun findByIdLocal() {
        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Car>>(
                ArgumentMatchers.eq(CarStateCountProcessorBinding.CAR_STORE),
                ArgumentMatchers.any())
        ).willReturn(carStore)

        val carId = "1"
        val car = CarFactory.createRandom(1)

        BDDMockito.given(carStore.get(carId)).willReturn(car)

        StepVerifier.create(carRepo.findByIdLocal(carId))
                .expectNext(car)
                .verifyComplete()
    }

    @Test
    fun notfoundByIdLocal() {
        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Car>>(
                ArgumentMatchers.eq(CarStateCountProcessorBinding.CAR_STORE),
                ArgumentMatchers.any())
        ).willReturn(carStore)

        val carId = "1"

        BDDMockito.given(carStore.get(carId)).willReturn(null)

        StepVerifier.create(carRepo.findByIdLocal(carId))
                .expectErrorMessage("car with id: $carId not found")
                .verify()
    }

    @Test
    fun findAllCarsLocal() {
        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Car>>(
                ArgumentMatchers.eq(CarStateCountProcessorBinding.CAR_STORE),
                ArgumentMatchers.any())
        ).willReturn(carStore)

        val car = CarFactory.createRandom(1)
        val iterator = createKeyValueItearor(listOf(KeyValue(car.getId(), car)).iterator())
        BDDMockito.given(carStore.all()).willReturn(iterator)

        StepVerifier.create(carRepo.findAllCarsLocal())
                .expectNext(car)
                .verifyComplete()
    }

    @Test
    fun getLocalCarsStateCounts() {
        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Long>>(
                ArgumentMatchers.eq(CarStateCountProcessorBinding.CAR_STATE_STORE),
                ArgumentMatchers.any())
        ).willReturn(carStateStore)

        val iterator = createKeyValueItearor(listOf(KeyValue("FREE", 1L)).iterator())
        BDDMockito.given(carStateStore.all()).willReturn(iterator)

        StepVerifier.create(carRepo.getLocalCarsStateCounts())
                .expectNext(mapOf("FREE" to 1L))
                .verifyComplete()
    }

    private fun <V> createKeyValueItearor(iterator: Iterator<KeyValue<String, V>>): KeyValueIterator<String, V> {
        return object : KeyValueIterator<String, V> {
            override fun next(): KeyValue<String, V> {
                return iterator.next()
            }

            override fun remove() {
            }

            override fun peekNextKey(): String {
                return "1"
            }

            override fun hasNext(): Boolean {
                return iterator.hasNext()
            }

            override fun close() {
            }

        }
    }
}
