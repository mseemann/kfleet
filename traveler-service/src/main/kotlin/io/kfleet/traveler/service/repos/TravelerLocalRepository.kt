package io.kfleet.traveler.service.repos


import io.kfleet.traveler.service.configuration.TRAVELER_RW_STORE
import io.kfleet.traveler.service.domain.Traveler
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


@Repository
class TravelerLocalRepository(private val interactiveQueryService: InteractiveQueryService) {

    fun findByIdLocal(travelerId: String): Mono<Traveler> =
            travelerStore().get(travelerId)?.toMono()
                    ?: Mono.error(Exception("traveler with id: $travelerId not found"))


    private fun travelerStore(): ReadOnlyKeyValueStore<String, Traveler> =
            interactiveQueryService
                    .getQueryableStore(TRAVELER_RW_STORE, QueryableStoreTypes.keyValueStore())


}
