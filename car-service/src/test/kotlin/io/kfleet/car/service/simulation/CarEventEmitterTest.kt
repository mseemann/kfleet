package io.kfleet.car.service.simulation

import io.kfleet.domain.events.car.CarDeregisteredEvent
import io.kfleet.domain.events.car.CarRegisteredEvent
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.test.util.ReflectionTestUtils
import reactor.test.StepVerifier

class CarEventEmitterTest {


    @Test
    fun noEmitCarEvents() {
        val carEventEmitter = CarEventEmitter()
        StepVerifier.create(carEventEmitter.emitCarEvents())
                .expectComplete()
                .verify()
    }

    @Test
    fun emitDemoCars() {
        val carEventEmitter = CarEventEmitter()

        ReflectionTestUtils.setField(carEventEmitter, "simulationEnabled", true)

        StepVerifier.create(carEventEmitter.emitCarEvents().take(1))
                .expectNextMatches {
                    when (val p = it.payload) {
                        is CarRegisteredEvent -> p.getCarId() == it.headers.get(KafkaHeaders.MESSAGE_KEY)
                        is CarDeregisteredEvent -> p.getCarId() == it.headers.get(KafkaHeaders.MESSAGE_KEY)
                        else -> throw RuntimeException("unknown car event")
                    }
                }
                .expectComplete()
                .verify()
    }
}
