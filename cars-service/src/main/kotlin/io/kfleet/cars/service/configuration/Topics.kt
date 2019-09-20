package io.kfleet.cars.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Topics {

    @Bean
    fun carsTopic() = NewTopic("cars", 3, 1)

    @Bean
    fun carCommandTopic() = NewTopic("car_commands", 3, 1)

    @Bean
    fun carEventTopic() = NewTopic("car_events", 3, 1)

    @Bean
    fun ownerCommandsTopic() = NewTopic("owner_commands", 3, 1)

    @Bean
    fun ownersTopic() = NewTopic("owners", 3, 1)
}
