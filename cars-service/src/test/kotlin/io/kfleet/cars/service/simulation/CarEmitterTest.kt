package io.kfleet.cars.service.simulation

import org.junit.jupiter.api.Test
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.test.util.ReflectionTestUtils
import reactor.test.StepVerifier

class CarEmitterTest {


    @Test
    fun noEmitDemoCars() {
        val carEmitter = CarEmitter()
        StepVerifier.create(carEmitter.emitCars())
                .expectComplete()
                .verify()
    }

    @Test
    fun emitDemoCars() {
        val carEmitter = CarEmitter()

        ReflectionTestUtils.setField(carEmitter, "simulationEnabled", true)

        StepVerifier.create(carEmitter.emitCars().take(1))
                .expectNextMatches {
                    it.payload.getId() == it.headers.get(KafkaHeaders.MESSAGE_KEY)
                }
                .expectComplete()
                .verify()
    }
}
