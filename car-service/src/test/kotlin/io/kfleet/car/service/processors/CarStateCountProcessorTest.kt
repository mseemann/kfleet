package io.kfleet.car.service.processors

import io.kfleet.car.service.domain.Car
import io.kfleet.car.service.domain.CarFactory
import io.kfleet.car.service.domain.CarState
import io.kfleet.car.service.processor.CarStateCountProcessorBinding
import io.kfleet.car.service.repos.CarsRepository
import io.kfleet.car.service.simulation.CarEventOutBindings
import io.kfleet.car.service.simulation.CarsOutBindings
import io.kfleet.common.configuration.ObjectMapperConfig
import io.kfleet.common.customRetry
import io.kfleet.common.headers
import io.kfleet.domain.events.carDeregisteredEvent
import io.kfleet.domain.events.carRegisteredEvent
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.awaitility.Durations.FIVE_HUNDRED_MILLISECONDS
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.withPollInterval
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.annotation.Import
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testing.KafkaContextInitializer
import kotlin.test.assertNotNull
import kotlin.test.expect

@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
@ExtendWith(SpringExtension::class)
@Import(ObjectMapperConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class CarStateCountProcessorTest {

    @Autowired
    @Output(CarsOutBindings.CARS)
    lateinit var carOuputChannel: MessageChannel

    @Autowired
    @Output(CarEventOutBindings.CARS)
    lateinit var carEventOutputChannel: MessageChannel

    @Autowired
    lateinit var carsRepository: CarsRepository


    @Test
    fun submitCar() {

        carsRepository.waitForStoreTobeQueryable(CarStateCountProcessorBinding.CAR_STORE, QueryableStoreTypes.keyValueStore<String, Car>())
        carsRepository.waitForStoreTobeQueryable(CarStateCountProcessorBinding.CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())


        val stats = carsRepository.getCarsStateCounts().block()
        assertNotNull(stats)
        expect(0) { stats.size }

        val carId = 1
        val car = CarFactory.createRandom(carId)
        val message = MessageBuilder.createMessage(car, headers(carId))
        val sended = carOuputChannel.send(message)
        // this must always be true - because for this output sync is false - e.g. not configured to be sync
        assert(true) { sended }


        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
            val respCar = carsRepository.findById("$carId").customRetry().block()

            expect(car.getState()) { respCar!!.getState() }
        }

        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
            val respCars = carsRepository.findAllCars().collectList().block()

            expect(1) { respCars!!.size }
        }

        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {

            val statsAfterPushMsg = carsRepository.getCarsStateCounts().block()!!

            expect(1L) { statsAfterPushMsg.get(car.getState().toString()) }
        }

    }

    @Test
    fun submitRegisterEvent() {
        val carId = 10
        val carEvent = carRegisteredEvent { setCarId("$carId") }
        val message = MessageBuilder.createMessage(carEvent, headers(carId))

        carEventOutputChannel.send(message)


        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
            val respCar = carsRepository.findById("$carId").customRetry().block()

            expect(CarState.FREE) { respCar!!.getState() }
        }

        val carDeregEvent = carDeregisteredEvent { setCarId("$carId") }
        val deregMessage = MessageBuilder.createMessage(carDeregEvent, headers(carId))

        carEventOutputChannel.send(deregMessage)

        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
            val respCar = carsRepository.findById("$carId").customRetry().block()

            expect(CarState.OUT_OF_POOL) { respCar!!.getState() }
        }

    }

}

