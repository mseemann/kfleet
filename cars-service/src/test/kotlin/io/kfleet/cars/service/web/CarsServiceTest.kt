package io.kfleet.cars.service.web

import io.kfleet.cars.service.configuration.JacksonObjectMapper
import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.domain.CarState
import io.kfleet.cars.service.domain.GeoPosition
import io.kfleet.cars.service.repos.CarsRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux


@WebFluxTest(CarsService::class)
@Import(JacksonObjectMapper::class, CarsRoutes::class)
class CarsServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: CarsRepository

    @Test
    fun getAllCarsTest() {
        val car = Car.newBuilder().apply {
            id = "1"
            state = CarState.FREE
            stateOfCharge = 0.5
            geoPosition = GeoPosition.newBuilder().apply {
                lng = 1.0
                lat = 2.0
            }.build()
        }.build()

        BDDMockito.given(repo.findAllCars()).willReturn(Flux.just(car))

        webClient.get().uri("/cars")
                .exchange()
                .expectStatus().isOk
                .expectBodyList(Car::class.java)
                .hasSize(1)
                .contains(car)
    }
}
