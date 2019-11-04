package io.kfleet.riderequestdispatcher.simulation

import org.junit.jupiter.api.Test
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.test.util.ReflectionTestUtils
import reactor.test.StepVerifier

class RideRequestedEventEmitterTest {

    @Test
    fun noEmitRideRequestEvents() {
        val rideRequestedEventEmitter = RideRequestedEventEmitter()
        StepVerifier.create(rideRequestedEventEmitter.emitRideRequestEvents())
                .expectComplete()
                .verify()
    }

    @Test
    fun emitRideRequestEvents() {
        val rideRequestedEventEmitter = RideRequestedEventEmitter()

        ReflectionTestUtils.setField(rideRequestedEventEmitter, "simulationEnabled", true)


        StepVerifier.create(rideRequestedEventEmitter.emitRideRequestEvents().take(4))
                .thenConsumeWhile {
                    it.payload.getFromGeoIndex() == it.headers.get(KafkaHeaders.MESSAGE_KEY)
                }
                .expectComplete()
                .verify()

    }
}
