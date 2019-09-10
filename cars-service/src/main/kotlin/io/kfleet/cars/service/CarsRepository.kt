package io.kfleet.cars.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import mu.KotlinLogging
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository

private val logger = KotlinLogging.logger {}


interface CarsBinding {

    companion object {
        const val CARS = "cars"
        const val CAR_STORE = "all-cars"
        const val CAR_STATE_STORE = "cars_by_state"
    }

    @Input(CARS)
    fun inputCars(): KTable<String, String>
}

@Repository
@EnableBinding(CarsBinding::class)
class CarsRepository {


    @Autowired
    lateinit var interactiveQueryService: InteractiveQueryService

    @Autowired
    lateinit var mapper: ObjectMapper

    @StreamListener
    fun carStateUpdates(@Input(CarsBinding.CARS) carTable: KTable<String, String>) {

        carTable
                .groupBy { _, rawCar: String ->
                    val car: Car = mapper.readValue(rawCar)
                    KeyValue(car.state.toString(), "")
                }
                .count(Materialized.`as`(CarsBinding.CAR_STATE_STORE))
                .toStream()
                .foreach { status: String, count: Long ->
                    logger.debug { "$status -> $count" }
                }
    }

    fun carsStore(): ReadOnlyKeyValueStore<String, String> = interactiveQueryService
            .getQueryableStore(CarsBinding.CAR_STORE, QueryableStoreTypes.keyValueStore<String, String>())


    fun carStateStore(): ReadOnlyKeyValueStore<String, Long> = interactiveQueryService
            .getQueryableStore(CarsBinding.CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())

}

