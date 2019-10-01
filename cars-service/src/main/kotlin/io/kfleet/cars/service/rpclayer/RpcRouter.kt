package io.kfleet.cars.service.rpclayer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

const val RPC_OWNER = "/rpc/owner"
const val RPC_CARS = "/rpc/cars"
const val RPC_COMMAND_RESPONSE = "/rpc/command-response"

@Configuration
class RpcRoutes(
        private val ownerLocalRpcService: OwnerLocalRpcService,
        private val carsLocalRpcService: CarsLocalRpcService,
        private val commandsResponseRpcService: CommandsResponseLocalRpcService) {

    @Bean
    fun rpcLocalApis() = router {
        ("/rpc").nest {
            ("/owner").nest {
                GET("/{ownerId}", ownerLocalRpcService::ownerById)
            }
            ("/cars").nest {
                GET("/", carsLocalRpcService::cars)
                GET("/stats", carsLocalRpcService::carsStateCount)
                GET("/{id}", carsLocalRpcService::carById)
            }
            ("/command-response").nest {
                GET("/{commandId}", commandsResponseRpcService::commandById)
            }
        }
    }

}
