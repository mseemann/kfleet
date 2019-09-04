package io.kfleet.monitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Car
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository

@Repository
@EnableBinding(CarBinding::class)
class CarRepository {

    val CAR_STOR = "all-cars"
    val CAR_STATE_STORE = "cars_by_state"

    @Autowired
    lateinit var interactiveQueryService: InteractiveQueryService

    val mapper = jacksonObjectMapper()

    @StreamListener
    fun carStateUpdates(@Input("cars") carTable: KTable<String, String>) {
        carTable.groupBy { key: String, rawCar: String ->
            val car: Car = mapper.readValue(rawCar)
            KeyValue(car.state.toString(), "")
        }
                .count(Materialized.`as`(CAR_STATE_STORE))
                .toStream()
                .foreach { a: String, c: Long ->
                    println("$a -> $c")
                }
    }

    fun allCarsStore() = interactiveQueryService
            .getQueryableStore(CAR_STOR, QueryableStoreTypes.keyValueStore<String, String>())


    fun allCarStateStore() = interactiveQueryService
            .getQueryableStore(CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())


}

interface CarBinding {
    @Input("cars")
    fun inputCars(): KTable<String, String>
}
