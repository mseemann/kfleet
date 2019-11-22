package io.kfleet.riderequestdispatcher.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime.now
import java.util.UUID.randomUUID
import java.util.function.BiFunction

data class Event(val id: String, val time: String)

@Component
class CarsWebSocketHandler(val objectMapper: ObjectMapper) : WebSocketHandler {
    

    private val eventFlux: Flux<String> = Flux.generate { sink ->
        sink.next(objectMapper.writeValueAsString(Event(randomUUID().toString(), now().toString())))
    }

    private val intervalFlux = Flux.interval(Duration.ofSeconds(1L))
            .zipWith(eventFlux, BiFunction<Long, String, String> { _, event -> event })

    override fun handle(webSocketSession: WebSocketSession): Mono<Void> {
        return webSocketSession
                .send(intervalFlux.map { webSocketSession.textMessage(it) })
                .and(webSocketSession.receive().map { it.payloadAsText }.log())
    }


}
