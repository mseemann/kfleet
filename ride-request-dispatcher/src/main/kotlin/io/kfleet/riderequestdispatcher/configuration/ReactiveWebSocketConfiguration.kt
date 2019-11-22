package io.kfleet.riderequestdispatcher.configuration

import io.kfleet.riderequestdispatcher.web.CarsWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter


@Configuration
class ReactiveWebSocketConfiguration(val carsWebSocketHandler: CarsWebSocketHandler) {
    
    @Bean
    fun webSocketHandlerMapping() = SimpleUrlHandlerMapping().apply {
        order = Ordered.HIGHEST_PRECEDENCE
        urlMap = mapOf("/cars" to carsWebSocketHandler)
    }

    @Bean
    fun handlerAdapter(): WebSocketHandlerAdapter = WebSocketHandlerAdapter()

}
