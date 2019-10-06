package io.kfleet.cars.service.repos

import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import reactor.test.StepVerifier

class OwnerLocalRepositoryTest {

    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var store: ReadOnlyKeyValueStore<String, Owner>;

    @InjectMocks
    lateinit var ownerRepo: OwnerLocalRepository

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun findByIdLocal() {
        given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Owner>>(
                ArgumentMatchers.eq(OwnerCommandsProcessorBinding.OWNER_RW_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val ownerId = "1"
        val owner = Owner.newBuilder().apply {
            id = ownerId
            name = "test"
        }.build()

        given(store.get(ownerId)).willReturn(owner)

        StepVerifier.create(ownerRepo.findByIdLocal(ownerId))
                .expectNext(owner)
                .verifyComplete()
    }

    @Test
    fun findByIdLocalNotFound() {
        given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Owner>>(
                ArgumentMatchers.eq(OwnerCommandsProcessorBinding.OWNER_RW_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val ownerId = "1"

        given(store.get(ownerId)).willReturn(null)

        StepVerifier.create(ownerRepo.findByIdLocal(ownerId))
                .expectErrorMessage("owner with id: $ownerId not found")
                .verify()
    }
}
