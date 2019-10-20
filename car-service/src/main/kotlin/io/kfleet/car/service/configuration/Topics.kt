package io.kfleet.car.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// TopicNames
const val CARS = "cars"
const val CAR_EVENTS = "car_events"
const val DLQ = "dlq"


// TopicBindingNames
const val CAR_EVENTS_IN = "car_events_in"


// StoreNames
const val CAR_RW_STORE = "car_store"


@Configuration
class Topics {

    @Bean
    fun carsTopic() = NewTopic(CARS, 3, 3)

    @Bean
    fun carEventTopic() = NewTopic(CAR_EVENTS, 3, 3)

    @Bean
    fun dlqTopic() = NewTopic(DLQ, 3, 3)
}
