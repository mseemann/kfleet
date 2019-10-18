package io.kfleet.owner.service.repos

import io.kfleet.owner.service.configuration.StoreNames
import io.kfleet.owner.service.domain.Owner
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


@Repository
class OwnerLocalRepository(private val interactiveQueryService: InteractiveQueryService) {

    fun findByIdLocal(ownerId: String): Mono<Owner> =
            ownerStore().get(ownerId)?.toMono() ?: Mono.error(Exception("owner with id: $ownerId not found"))


    private fun ownerStore(): ReadOnlyKeyValueStore<String, Owner> =
            interactiveQueryService
                    .getQueryableStore(StoreNames.OWNER_RW_STORE, QueryableStoreTypes.keyValueStore<String, Owner>())


}
