package io.kfleet.car.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Topics {

    @Bean
    fun carsTopic() = NewTopic("cars", 3, 3)

    @Bean
    fun carCommandTopic() = NewTopic("car_commands", 3, 3)

    @Bean
    fun carEventTopic() = NewTopic("car_events", 3, 3)


}
