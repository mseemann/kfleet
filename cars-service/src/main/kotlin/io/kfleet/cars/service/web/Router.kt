package io.kfleet.cars.service.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class Routes(private val ownerService: OwnerService) {

    @Bean
    fun apis() = router {
        ("/owners").nest {
            GET("/{id}", ownerService::ownerById)
            POST("/{ownerId}/{ownerName}", ownerService::createOwner)
            PUT("/{ownerId}/{ownerName}", ownerService::updateOwnersName)
        }
    }

}


