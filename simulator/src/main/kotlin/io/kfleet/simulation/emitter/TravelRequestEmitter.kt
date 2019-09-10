package io.kfleet.simulation.emitter


import io.kfleet.domain.TravelRequest
import mu.KotlinLogging
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.reactive.StreamEmitter
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Flux

private val logger = KotlinLogging.logger {}

@EnableBinding(TravelRequestBindings::class)
class TravelRequestEmitter {

    @StreamEmitter
    @Output(TravelRequestBindings.TRAVEL_REQUESTS)
    fun emitTravelRequests(): Flux<Message<TravelRequest>> = randomDelayFluxer(TRAVELER_COUNT, sleepFrom = 10, sleepUntil = 30).map {
        // TODO make a lookup fo a Traveler in State IS_LIVING and create a TravelRequest for him
        val travelRequest = TravelRequest.create(it)
        logger.debug { "emit: $travelRequest" }
        MessageBuilder.createMessage(travelRequest, headers(it))
    }

}

interface TravelRequestBindings {

    companion object {
        const val TRAVEL_REQUESTS = "travel-requests"
    }

    @Output(TRAVEL_REQUESTS)
    fun travelRequests(): MessageChannel
}
