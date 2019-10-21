package io.kfleet.traveler.service.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class TravelerRoutes(private val travelerService: TravelerService) {

    @Bean
    fun travelerApis() = router {
        ("/traveler").nest {
            GET("/{id}", travelerService::travelerById)
            POST("", travelerService::createTraveler)
            DELETE("/{travelerId}", travelerService::deleteTraveler)
        }
    }

}
