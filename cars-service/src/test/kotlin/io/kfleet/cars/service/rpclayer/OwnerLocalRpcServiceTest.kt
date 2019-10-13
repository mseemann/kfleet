package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.configuration.JacksonObjectMapper
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.owner
import io.kfleet.cars.service.repos.OwnerLocalRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import kotlin.test.expect

@WebFluxTest(OwnerLocalRpcService::class)
@Import(JacksonObjectMapper::class, OwnerRpcRoutes::class)
class OwnerLocalRpcServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: OwnerLocalRepository

    @Test
    fun ownerByIdTest() {
        val owner = owner {
            id = "1"
            name = "test"
        }

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.just(owner))

        val response = webClient.get().uri("/rpc/owner/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(Owner::class.java)
                .returnResult()

        expect(owner) { response.responseBody }
    }

    @Test
    fun ownerByIdTest404() {

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/rpc/owner/1")
                .exchange()
                .expectStatus().isNotFound
    }
}
