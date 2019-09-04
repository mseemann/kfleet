package io.kfleet.validator

import TravelRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Traveler
import io.kfleet.domain.TravelerStatus
import io.kfleet.validator.configuration.TravelerBinding
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.cloud.stream.messaging.Processor
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder

@EnableBinding(Processor::class, TravelerBinding::class)
class TravelRequestValidator {

    @Autowired
    lateinit var interactiveQueryService: InteractiveQueryService

    val mapper = jacksonObjectMapper()

    // this is not save. no tx and no exactly once guarantee - should be a stream processor! But is here
    // for demonstration purposes
    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    fun onInput(travelRequest: TravelRequest): Message<TravelRequest> {
        println("TravelRequest: $travelRequest")

        val travelerStore = interactiveQueryService
                .getQueryableStore("all-travelers", QueryableStoreTypes.keyValueStore<String, String>())

        val traveler = travelerStore.get(travelRequest.personId)?.let { mapper.readValue<Traveler>(it) }

        traveler?.let {
            if (traveler.state != TravelerStatus.IS_LIVING) {
                throw Exception("traveler: ${traveler.id} has the wrong state ${traveler.state}")
            }

            // it would be possible to partition this topic by key that is based on the geoLocations
            return MessageBuilder.createMessage(travelRequest, MessageHeaders(mapOf(KafkaHeaders.MESSAGE_KEY to travelRequest.personId)))
        }

        throw Exception("traveler: ${travelRequest.personId} not found")
    }

    // this listener is required so that spring cloud stream creates the  binder and the store
    // with the travelers
    @StreamListener
    fun test(@Input("travelers") carTable: KTable<String, String>) {
    }
}
