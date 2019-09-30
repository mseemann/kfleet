package io.kfleet.cars.service.repos

import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono


@Repository
class OwnerLocalRepository(
        @Autowired private val interactiveQueryService: InteractiveQueryService) {

    fun findByIdLocal(ownerId: String): Mono<Owner> {
        return ownerStore().get(ownerId)?.toMono() ?: Mono.error(Exception("owner with id: $ownerId not found"))
    }

    private fun ownerStore(): ReadOnlyKeyValueStore<String, Owner> = interactiveQueryService
            .getQueryableStore(OwnerCommandsProcessorBinding.OWNER_STORE, QueryableStoreTypes.keyValueStore<String, Owner>())

}
