package io.kfleet.car.service.processors

import io.kfleet.car.service.domain.Car
import io.kfleet.car.service.domain.CarFactory
import io.kfleet.car.service.processor.CarStateCountProcessorBinding
import io.kfleet.car.service.repos.CarsRepository
import io.kfleet.car.service.simulation.CarsOutBindings
import io.kfleet.common.headers
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
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testing.KafkaContextInitializer
import kotlin.test.assertNotNull
import kotlin.test.expect

@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class ProcessorIntegrationTests {

    @Autowired
    @Output(CarsOutBindings.CARS)
    lateinit var carOuputChannel: MessageChannel

    @Autowired
    lateinit var carsRepository: CarsRepository


    @Test
    fun submitCar() {

        val stats = carsRepository.getCarsStateCounts().block()
        assertNotNull(stats)
        expect(0) { stats.size }

        val carId = 1
        val car = CarFactory.createRandom(carId)
        val message = MessageBuilder.createMessage(car, headers(carId))
        val sended = carOuputChannel.send(message)
        // this must always be true - because for this output sync is false - e.g. not configured to be sync
        assert(true) { sended }

        Thread.sleep(30000)

        carsRepository.waitForStoreTobeQueryable(CarStateCountProcessorBinding.CAR_STORE, QueryableStoreTypes.keyValueStore<String, Car>())
        carsRepository.waitForStoreTobeQueryable(CarStateCountProcessorBinding.CAR_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())

        await withPollInterval FIVE_HUNDRED_MILLISECONDS untilAsserted {
            val respCar = carsRepository.findById("$carId").block()

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
}


