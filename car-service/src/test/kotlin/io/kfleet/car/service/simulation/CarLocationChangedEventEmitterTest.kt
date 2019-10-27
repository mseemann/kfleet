package io.kfleet.car.service.simulation

import io.kfleet.domain.events.car.CarLocationChangedEvent
import org.junit.jupiter.api.Test
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.test.util.ReflectionTestUtils
import reactor.test.StepVerifier

class CarLocationChangedEventEmitterTest {

    @Test
    fun noEmitCarLocationChangedEvents() {
        val carEventEmitter = CarLocationChangedEventEmitter()
        StepVerifier.create(carEventEmitter.emitCarLocations())
                .expectComplete()
                .verify()
    }

    @Test
    fun emitLocationChanged() {
        val carEventEmitter = CarLocationChangedEventEmitter()

        ReflectionTestUtils.setField(carEventEmitter, "simulationEnabled", true)

        for (i in 1..10) {
            StepVerifier.create(carEventEmitter.emitCarLocations().take(1))
                    .expectNextMatches {
                        when (val p = it.payload) {
                            is CarLocationChangedEvent -> p.getCarId() == it.headers.get(KafkaHeaders.MESSAGE_KEY)
                            else -> throw RuntimeException("unknown car location changed event")
                        }
                    }
                    .expectComplete()
                    .verify()
        }
    }
}
