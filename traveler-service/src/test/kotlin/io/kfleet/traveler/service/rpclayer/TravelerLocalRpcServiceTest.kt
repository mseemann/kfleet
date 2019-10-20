package io.kfleet.traveler.service.rpclayer


import io.kfleet.traveler.service.domain.Traveler
import io.kfleet.traveler.service.domain.traveler
import io.kfleet.traveler.service.repos.TravelerLocalRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import kotlin.test.expect

@WebFluxTest(TravelerLocalRpcService::class)
@Import(TravelerRpcRoutes::class)
class TravelerLocalRpcServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: TravelerLocalRepository

    @Test
    fun travelerByIdTest() {
        val traveler = traveler {
            id = "1"
            name = "test"
            email = "a@a.com"
        }

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.just(traveler))

        val response = webClient.get().uri("/rpc/traveler/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(Traveler::class.java)
                .returnResult()

        expect(traveler) { response.responseBody }
    }

    @Test
    fun travelerByIdTest404() {

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/rpc/traveler/1")
                .exchange()
                .expectStatus().isNotFound
    }
}
