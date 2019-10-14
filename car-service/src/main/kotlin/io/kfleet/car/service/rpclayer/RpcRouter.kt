package io.kfleet.car.service.rpclayer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

const val RPC_CARS = "/rpc/cars"


@Configuration
class CarsRpcRoutes(private val carsLocalRpcService: CarsLocalRpcService) {

    @Bean
    fun rpcCarsLocalApis() = router {
        ("/rpc").nest {
            ("/cars").nest {
                GET("/", carsLocalRpcService::cars)
                GET("/stats", carsLocalRpcService::carsStateCount)
                GET("/{id}", carsLocalRpcService::carById)
            }
        }
    }

}
