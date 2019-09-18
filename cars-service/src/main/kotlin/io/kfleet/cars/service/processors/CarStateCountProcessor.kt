package io.kfleet.cars.service.processors

import io.kfleet.cars.service.domain.Car
import mu.KotlinLogging
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener

private val logger = KotlinLogging.logger {}

interface CarStateCountProcessorBinding {

    companion object {
        const val CARS = "cars"
        const val CAR_STORE = "all-cars"
        const val CAR_STATE_STORE = "cars_by_state"
    }

    @Input(CARS)
    fun inputCars(): KTable<String, Car>

}

@EnableBinding(CarStateCountProcessorBinding::class)
class CarStateCountProcessor() {

    @StreamListener
    fun carStateUpdates(@Input(CarStateCountProcessorBinding.CARS) carTable: KTable<String, Car>) {

        carTable
                .groupBy { _, car: Car ->
                    KeyValue(car.getState().toString(), "")
                }
                .count(Materialized.`as`(CarStateCountProcessorBinding.CAR_STATE_STORE))
                .toStream()
                .foreach { status: String, count: Long ->
                    logger.debug { "$status -> $count" }
                }
    }
}
