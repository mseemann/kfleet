package io.kfleet.cars.service.rpclayer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

@Configuration
class RpcRoutes(private val ownerLocalRpcService: OwnerLocalRpcService) {

    @Bean
    fun rpcLocalApis() = router {
        ("/$OWNER_RPC").nest {
            GET("/{ownerId}", ownerLocalRpcService::ownerById)
        }
    }

}
