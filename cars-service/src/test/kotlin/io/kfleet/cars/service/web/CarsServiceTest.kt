package io.kfleet.cars.service.web

import io.kfleet.cars.service.configuration.JacksonObjectMapper
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarFactory
import io.kfleet.cars.service.domain.CarState
import io.kfleet.cars.service.domain.GeoPositionFactory
import io.kfleet.cars.service.repos.CarsRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import kotlin.test.expect


@WebFluxTest(CarsService::class)
@Import(JacksonObjectMapper::class, CarsRoutes::class)
@AutoConfigureWebTestClient(timeout = "15001") // backof retry is between 1 and 3 seconds; 5 times
class CarsServiceTest {

    private val car = CarFactory.create(
            id = "1",
            state = CarState.FREE,
            stateOfCharge = 0.5,
            geoPosition = GeoPositionFactory.create(lng = 1.0, lat = 2.0)
    )

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: CarsRepository

    @Test
    fun getAllCarsTest() {

        BDDMockito.given(repo.findAllCars()).willReturn(Flux.just(car))

        webClient.get().uri("/cars")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBodyList(Car::class.java)
                .hasSize(1)
                .contains(car)
    }

    @Test
    fun getCarById() {

        BDDMockito.given(repo.findById("1")).willReturn(Mono.just(car))

        val response = webClient.get().uri("/cars/1")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody(Car::class.java)
                .returnResult()

        expect(response.responseBody) { car }
    }

    @Test
    fun getCarById404() {

        BDDMockito.given(repo.findById("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/cars/1")
                .exchange()
                .expectStatus().isNotFound

    }

    @Test
    fun getStatistics() {
        val statistic = mapOf(CarState.FREE.toString() to 1L)

        BDDMockito.given(repo.getCarsStateCounts()).willReturn(Mono.just(statistic))

        webClient.get().uri("/cars/stats")
                .exchange()
                .expectStatus().isOk
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.FREE").isEqualTo("1")

    }
}
