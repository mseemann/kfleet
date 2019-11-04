package io.kfleet.car.service.domain

import io.kfleet.car.service.processor.CarAndEvent
import io.kfleet.domain.events.GeoPositionFactory
import io.kfleet.domain.events.carDeregisteredEvent
import io.kfleet.domain.events.carRegisteredEvent
import io.kfleet.domain.events.ownerCreatedEvent
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class CarProcessorTest {

    private val carProcessor = CarProcessor()

    @Test
    fun registerCarTest() {
        val carId = "1"

        val event = carRegisteredEvent {
            setCarId(carId)
        }

        val result = carProcessor.processEvent(CarAndEvent(null, event))

        expect(1) { result.count() }
        expect(carId) { result[0].key }
        expect(carId) { (result[0].value as Car).getId() }
        expect(CarState.FREE) { (result[0].value as Car).getState() }
    }

    @Test
    fun reRegisterCarTest() {
        val carId = "1"

        val event = carRegisteredEvent {
            setCarId(carId)
        }

        val car = car {
            id = carId
            state = CarState.OUT_OF_POOL
            geoPosition = GeoPositionFactory.createRandomCarLocation().toCarLocation()
            stateOfCharge = 100.0
        }

        val result = carProcessor.processEvent(CarAndEvent(car, event))

        expect(1) { result.count() }
        expect(carId) { result[0].key }
        expect(carId) { (result[0].value as Car).getId() }
        expect(CarState.FREE) { (result[0].value as Car).getState() }
    }

    @Test
    fun reRegisterFreeCarTest() {
        val carId = "1"

        val event = carRegisteredEvent {
            setCarId(carId)
        }

        val car = car {
            id = carId
            state = CarState.FREE
            geoPosition = GeoPositionFactory.createRandomCarLocation().toCarLocation()
            stateOfCharge = 100.0
        }

        val result = carProcessor.processEvent(CarAndEvent(car, event))

        expect(0) { result.count() }
    }

    @Test
    fun deregisterNonExistingCarTest() {
        val carId = "1"

        val event = carDeregisteredEvent {
            setCarId(carId)
        }

        val result = carProcessor.processEvent(CarAndEvent(null, event))

        expect(0) { result.count() }
    }

    @Test
    fun deregisterOutOfPoolCarTest() {
        val carId = "1"

        val event = carDeregisteredEvent {
            setCarId(carId)
        }

        val car = car {
            id = carId
            state = CarState.OUT_OF_POOL
            geoPosition = GeoPositionFactory.createRandomCarLocation().toCarLocation()
            stateOfCharge = 100.0
        }

        val result = carProcessor.processEvent(CarAndEvent(car, event))

        expect(0) { result.count() }
    }

    @Test
    fun deregisterCarTest() {
        val carId = "1"

        val event = carDeregisteredEvent {
            setCarId(carId)
        }

        val car = car {
            id = carId
            state = CarState.FREE
            geoPosition = GeoPositionFactory.createRandomCarLocation().toCarLocation()
            stateOfCharge = 100.0
        }

        val result = carProcessor.processEvent(CarAndEvent(car, event))

        expect(1) { result.count() }
        expect(carId) { result[0].key }
        expect(carId) { (result[0].value as Car).getId() }
        expect(CarState.OUT_OF_POOL) { (result[0].value as Car).getState() }
    }

    @Test
    fun testUnknownEventException() {
        // this is not a car event!!
        val command = ownerCreatedEvent {
            ownerId = "1"
            name = " name"
        }
        assertFailsWith(RuntimeException::class) {
            carProcessor.processEvent(CarAndEvent(null, command))
        }
    }
}
