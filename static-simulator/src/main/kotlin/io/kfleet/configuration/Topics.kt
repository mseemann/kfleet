package io.kfleet.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Topics {

    @Bean
    fun travelerTopic() = NewTopic("travelers", 3, 1)

    @Bean
    fun carsTopic() = NewTopic("cars", 3, 1)
}
