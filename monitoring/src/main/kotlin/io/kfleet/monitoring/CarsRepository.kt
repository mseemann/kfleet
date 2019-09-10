package io.kfleet.monitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Car
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

const val CAR_STORE = "all-cars"
const val CAR_STATE_STORE = "cars_by_state"

interface CarsBinding {
    @Input("cars")
    fun inputCars(): KTable<String, String>
}

@Repository
@EnableBinding(CarsBinding::class)
class CarsRepository {


    @Autowired
    lateinit var interactiveQueryService: InteractiveQueryService

    val mapper = jacksonObjectMapper()

    @StreamListener
    fun carStateUpdates(@Input("cars") carTable: KTable<String, String>) {

        carTable
                .groupBy { _, rawCar: String ->
                    val car: Car = mapper.readValue(rawCar)
                    KeyValue(car.state.toString(), "")
                }
                .count(Materialized.`as`(CAR_STATE_STORE))
                .toStream()
                .foreach { status: String, count: Long ->
                    logger.debug { "$status -> $count" }
                }
    }

    fun carsStore(): ReadOnlyKeyValueStore<String, String> = interactiveQueryService
            .getQueryableStore(CAR_STORE, QueryableStoreTypes.keyValueStore<String, String>())


    fun carStateStore(): ReadOnlyKeyValueStore<String, Long> = interactiveQueryService
            .getQueryableStore(CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())

}

