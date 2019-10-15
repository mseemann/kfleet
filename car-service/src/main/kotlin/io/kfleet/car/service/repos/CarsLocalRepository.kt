package io.kfleet.car.service.repos

import io.kfleet.car.service.domain.Car
import io.kfleet.car.service.processor.CarStateCountProcessorBinding
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono

@Repository
class CarsLocalRepository(private val interactiveQueryService: InteractiveQueryService) {

    fun findByIdLocal(id: String): Mono<Car> {
        return carsStore().get(id)?.toMono() ?: Mono.error(Exception("car with id: $id not found"))
    }

    fun findAllCarsLocal(): Flux<Car> {
        return carsStore().all().use {
            it.asSequence().map { kv -> kv.value }.toList().toFlux()
        }
    }

    fun getLocalCarsStateCounts(): Mono<Map<String, Long>> {
        return carStateStore().all().use { allCars ->
            allCars.asSequence().map { it.key to it.value }.toMap()
        }.toMono()
    }


    private fun carsStore(): ReadOnlyKeyValueStore<String, Car> = interactiveQueryService
            .getQueryableStore(CarStateCountProcessorBinding.CAR_STORE, QueryableStoreTypes.keyValueStore<String, Car>())


    private fun carStateStore(): ReadOnlyKeyValueStore<String, Long> = interactiveQueryService
            .getQueryableStore(CarStateCountProcessorBinding.CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())

}
