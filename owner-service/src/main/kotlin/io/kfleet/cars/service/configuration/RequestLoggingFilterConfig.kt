package io.kfleet.cars.service.configuration

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.server.WebFilter

private val log = KotlinLogging.logger {}

@Configuration
class RequestLoggingFilterConfig {

    @Bean
    fun loggingFilter(): WebFilter = WebFilter { exchange, chain ->
        val request = exchange.request
        log.info { "request method=${request.method} path=${request.path.pathWithinApplication()} params=[${request.queryParams}] body=[${request.body}]" }

        val result = chain.filter(exchange)

        log.info { "response code=${exchange.response.statusCode}" }

        return@WebFilter result
    }
}
