package io.kfleet.cars.service

import io.kfleet.common.createWebClient
import org.apache.kafka.streams.state.HostInfo
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class WebClientUtil {

    fun <T> doGet(hostInfo: HostInfo, uri: String, clazz: Class<T>): Mono<T> {
        return createWebClient(hostInfo).get().uri(uri).retrieve().bodyToMono(clazz)
    }

    fun <T> doGetFlux(hostInfo: HostInfo, uri: String, clazz: Class<T>): Flux<T> {
        return createWebClient(hostInfo).get().uri(uri).retrieve().bodyToFlux(clazz)
    }
}
