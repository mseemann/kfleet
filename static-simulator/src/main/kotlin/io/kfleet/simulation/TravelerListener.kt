package io.kfleet.simulation

import io.kfleet.domain.Traveler
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.messaging.handler.annotation.Headers

@EnableBinding(Sink::class)
class TravelerListener {

    @StreamListener(Sink.INPUT)
    fun onInput(
        traveler: Traveler,
        @Headers() headers: Map<String, String>
    ) = println("Traveler: $traveler with headers: $headers")
}
