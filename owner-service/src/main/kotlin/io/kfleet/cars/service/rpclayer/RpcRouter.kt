package io.kfleet.cars.service.rpclayer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

const val RPC_OWNER = "/rpc/owner"
const val RPC_CARS = "/rpc/cars"
const val RPC_COMMAND_RESPONSE = "/rpc/command-response"

@Configuration
class OwnerRpcRoutes(private val ownerLocalRpcService: OwnerLocalRpcService) {

    @Bean
    fun rpcOwnerLocalApis() = router {
        ("/rpc").nest {
            ("/owner").nest {
                GET("/{ownerId}", ownerLocalRpcService::ownerById)
            }
        }
    }

}

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

@Configuration
class CommandsResponseRpcRoutes(
        private val commandsResponseRpcService: CommandsResponseLocalRpcService) {

    @Bean
    fun rpcCommandResponseLocalApis() = router {
        ("/rpc").nest {
            ("/command-response").nest {
                GET("/{commandId}", commandsResponseRpcService::commandById)
            }
        }
    }

}
