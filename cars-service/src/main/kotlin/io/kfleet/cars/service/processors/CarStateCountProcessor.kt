package io.kfleet.cars.service.processors

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import mu.KotlinLogging
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.springframework.beans.factory.annotation.Autowired
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
    fun inputCars(): KTable<String, String>

}

@EnableBinding(CarStateCountProcessorBinding::class)
class CarStateCountProcessor(@Autowired val mapper: ObjectMapper) {

    @StreamListener
    fun carStateUpdates(@Input(CarStateCountProcessorBinding.CARS) carTable: KTable<String, String>) {

        carTable
                .groupBy { _, rawCar: String ->
                    val car: Car = mapper.readValue(rawCar)
                    KeyValue(car.state.toString(), "")
                }
                .count(Materialized.`as`(CarStateCountProcessorBinding.CAR_STATE_STORE))
                .toStream()
                .foreach { status: String, count: Long ->
                    logger.debug { "$status -> $count" }
                }
    }
}
