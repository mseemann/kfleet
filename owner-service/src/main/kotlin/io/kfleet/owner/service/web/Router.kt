package io.kfleet.owner.service.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class OwnerRoutes(private val ownerService: OwnerService) {

    @Bean
    fun ownerApis() = router {
        ("/owners").nest {
            GET("/{id}", ownerService::ownerById)
            POST("/{ownerId}/{ownerName}", ownerService::createOwner)
            PUT("/{ownerId}/{ownerName}", ownerService::updateOwnersName)
            DELETE("/{ownerId}", ownerService::deleteOwner)
            POST("/{ownerId}/car/register", ownerService::registerACar)
            POST("/{ownerId}/car/{carId}/deregister", ownerService::deregisterACar)
        }
    }

}
