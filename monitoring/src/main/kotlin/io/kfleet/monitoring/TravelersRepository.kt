package io.kfleet.monitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Traveler
import mu.KotlinLogging
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository

private val logger = KotlinLogging.logger {}

const val TRAVELER_STORE = "all-travelers"
const val TRAVELER_STATE_STORE = "travelers_by_state"

interface TravelersBinding {
    @Input("travelers")
    fun inputTravelers(): KTable<String, String>
}

@Repository
@EnableBinding(TravelersBinding::class)
class TravelersRepository {


    @Autowired
    lateinit var interactiveQueryService: InteractiveQueryService

    val mapper = jacksonObjectMapper()

    @StreamListener
    fun travelerStateUpdates(@Input("travelers") travelerTable: KTable<String, String>) {

        travelerTable
                .groupBy { _, rawTraveler: String ->
                    val traveler: Traveler = mapper.readValue(rawTraveler)
                    KeyValue(traveler.state.toString(), "")
                }
                .count(Materialized.`as`(TRAVELER_STATE_STORE))
                .toStream()
                .foreach { a: String, c: Long ->
                    logger.debug { "$a -> $c" }
                }
    }

    fun travelersStore(): ReadOnlyKeyValueStore<String, String> = interactiveQueryService
            .getQueryableStore(TRAVELER_STORE, QueryableStoreTypes.keyValueStore<String, String>())


    fun travelersStateStore(): ReadOnlyKeyValueStore<String, Long> = interactiveQueryService
            .getQueryableStore(TRAVELER_STATE_STORE, QueryableStoreTypes.keyValueStore<String, Long>())


}

