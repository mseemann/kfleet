package io.kfleet.car.service.domain

import io.kfleet.car.service.processor.CarAndEvent
import io.kfleet.domain.events.car.CarDeregisteredEvent
import io.kfleet.domain.events.car.CarRegisteredEvent
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}


@Component
class CarProcessor {

    fun processEvent(carAndEvent: CarAndEvent): List<KeyValue<String, SpecificRecord?>> {
        log.debug { "process Car and Event: $carAndEvent" }

        return when (val event = carAndEvent.carEvent) {
            is CarRegisteredEvent -> registerCar(event, carAndEvent.car)
            is CarDeregisteredEvent -> deregisterCar(event, carAndEvent.car)
            else -> throw RuntimeException("unsupported event: ${event::class}")
        }
    }

    private fun registerCar(event: CarRegisteredEvent, existingCar: Car?): List<KeyValue<String, SpecificRecord?>> {
        if (existingCar == null) {
            // if there is no car yet present - create one
            return listOf(
                    car {
                        id = event.getCarId()
                        state = CarState.FREE
                        stateOfCharge = 1.0
                        geoPosition = GeoPositionFactory.createRandom()
                    }.asKeyValue()
            )
        }

        // if the car state is out_of_pool mark it as free
        if (existingCar.getState() == CarState.OUT_OF_POOL) {
            existingCar.setState(CarState.FREE)
            return listOf(existingCar.asKeyValue())
        }

        return listOf()
    }

    private fun deregisterCar(event: CarDeregisteredEvent, existingCar: Car?): List<KeyValue<String, SpecificRecord?>> {
        if (existingCar != null && existingCar.getState() != CarState.OUT_OF_POOL) {
            existingCar.setState(CarState.OUT_OF_POOL)
            return listOf(existingCar.asKeyValue())
        }
        return listOf()
    }
}
