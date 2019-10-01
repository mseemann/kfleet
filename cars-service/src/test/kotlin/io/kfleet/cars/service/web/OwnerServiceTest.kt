package io.kfleet.cars.service.web

import io.kfleet.cars.service.configuration.JacksonObjectMapper
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.repos.CommandsResponseRepository
import io.kfleet.cars.service.repos.OwnerRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import kotlin.test.expect

@WebFluxTest(OwnerService::class)
@Import(JacksonObjectMapper::class, OwnerRoutes::class)
class OwnerServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: OwnerRepository

    @MockBean
    private lateinit var commandResponseRepo: CommandsResponseRepository

    @Test
    fun ownerByIdTest() {
        val owner = Owner.newBuilder().apply {
            id = "1"
            name = "testname"
        }.build()

        BDDMockito.given(repo.findById("1")).willReturn(Mono.just(owner))

        val response = webClient.get().uri("/owners/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(Owner::class.java)
                .returnResult()

        expect(response.responseBody) { owner }
    }

}
