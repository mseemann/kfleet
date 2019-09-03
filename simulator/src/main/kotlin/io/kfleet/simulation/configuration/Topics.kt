package io.kfleet.simulation.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Topics {

    @Bean
    fun travelersTopic() = NewTopic("travelers", 3, 1)

    @Bean
    fun carsTopic() = NewTopic("cars", 3, 1)

    @Bean
    fun travelRequestsTopic() = NewTopic("travel_requests", 3, 1)

    @Bean
    fun acceptedTravelRequestsTopic() = NewTopic("accepted_travel_requests", 3, 1)
}
