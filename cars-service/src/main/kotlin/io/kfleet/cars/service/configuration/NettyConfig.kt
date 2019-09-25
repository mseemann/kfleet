package io.kfleet.cars.service.configuration

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.stereotype.Component


@Component
class NettyWebServerFactoryPortCustomizer : WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

    override fun customize(serverFactory: NettyReactiveWebServerFactory) {
        println(serverFactory.port)
        //serverFactory.setPort(8084)
    }
}
