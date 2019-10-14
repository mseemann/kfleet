package io.kfleet.owner.service.rpclayer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

const val RPC_OWNER = "/rpc/owner"
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
