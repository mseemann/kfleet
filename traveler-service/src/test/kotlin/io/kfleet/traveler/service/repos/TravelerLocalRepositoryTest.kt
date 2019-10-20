package io.kfleet.traveler.service.repos

import io.kfleet.traveler.service.configuration.TRAVELER_RW_STORE
import io.kfleet.traveler.service.domain.Traveler
import io.kfleet.traveler.service.domain.traveler
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

class TravelerLocalRepositoryTest {

    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var store: ReadOnlyKeyValueStore<String, Traveler>

    @InjectMocks
    lateinit var travelerRepo: TravelerLocalRepository

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun findByIdLocal() {
        given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Traveler>>(
                ArgumentMatchers.eq(TRAVELER_RW_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val travelerId = "1"
        val traveler = traveler {
            id = travelerId
            name = "test"
            email = "a@a.com"
        }

        given(store.get(travelerId)).willReturn(traveler)

        StepVerifier.create(travelerRepo.findByIdLocal(travelerId))
                .expectNext(traveler)
                .verifyComplete()
    }

    @Test
    fun findByIdLocalNotFound() {
        given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, Traveler>>(
                ArgumentMatchers.eq(TRAVELER_RW_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val travelerId = "1"

        given(store.get(travelerId)).willReturn(null)

        StepVerifier.create(travelerRepo.findByIdLocal(travelerId))
                .expectErrorMessage("traveler with id: $travelerId not found")
                .verify()
    }
}
