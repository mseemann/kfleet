package io.kfleet.car.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

object TopicNames {
    const val CARS = "cars"
    const val CAR_EVENTS = "car_events"
    const val DLQ = "dlq"
}

object TopicBindingNames {
    const val CAR_EVENTS_IN = "car_events_in"
}

@Configuration
class Topics {

    @Bean
    fun carsTopic() = NewTopic(TopicNames.CARS, 3, 3)

    @Bean
    fun carEventTopic() = NewTopic(TopicNames.CAR_EVENTS, 3, 3)

    @Bean
    fun dlqTopic() = NewTopic(TopicNames.DLQ, 3, 3)
}
