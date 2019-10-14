package io.kfleet.car.service.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router


@Configuration
class CarsRoutes(private val carsService: CarsService) {

    @Bean
    fun carsApis() = router {
        ("/cars").nest {
            GET("", carsService::cars)
            GET("/stats", carsService::carsStateCount)
            GET("/{id}", carsService::carById)

        }
    }

}
