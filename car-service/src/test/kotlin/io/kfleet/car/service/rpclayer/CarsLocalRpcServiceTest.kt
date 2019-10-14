package io.kfleet.car.service.rpclayer

import io.kfleet.car.service.configuration.JacksonObjectMapper
import io.kfleet.car.service.domain.CarFactory
import io.kfleet.car.service.repos.CarsLocalRepository
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.expect

@WebFluxTest(CarsLocalRpcService::class)
@Import(JacksonObjectMapper::class, CarsRpcRoutes::class)
class CarsLocalRpcServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: CarsLocalRepository

    @Test
    fun carByIdTest() {

        val car = CarFactory.createRandom(1)

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.just(car))

        val response = webClient.get().uri("/rpc/cars/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(Car::class.java)
                .returnResult()

        expect(car) { response.responseBody }
    }

    @Test
    fun carByIdTest404() {

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/rpc/cars/1")
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun getAllCarsTest() {
        val car = CarFactory.createRandom(1)

        BDDMockito.given(repo.findAllCarsLocal()).willReturn(Flux.just(car))

        webClient.get().uri("/rpc/cars")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Car::class.java)
                .hasSize(1)
                .contains(car)
    }

    @Test
    fun getStatistics() {
        val statistic = mapOf(CarState.FREE.toString() to 1L)

        BDDMockito.given(repo.getLocalCarsStateCounts()).willReturn(Mono.just(statistic))

        webClient.get().uri("/rpc/cars/stats")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.FREE").isEqualTo("1")

    }
}
