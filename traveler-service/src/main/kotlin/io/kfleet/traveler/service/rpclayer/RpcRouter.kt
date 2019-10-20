package io.kfleet.traveler.service.rpclayer

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.router

const val RPC_TRAVELER = "/rpc/traveler"
const val RPC_COMMAND_RESPONSE = "/rpc/command-response"

@Configuration
class TravelerRpcRoutes(private val travelerLocalRpcService: TravelerLocalRpcService) {

    @Bean
    fun rpcTravelerLocalApis() = router {
        ("/rpc").nest {
            ("/traveler").nest {
                GET("/{travelerId}", travelerLocalRpcService::travelerById)
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
