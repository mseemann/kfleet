package io.kfleet.car.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


// TopicBindingNames
const val CAR_EVENTS_IN = "car_events_in"

const val RIDE_REQUEST_EVENTS = "ride_request_events"

@Configuration
class Topics {


    @Bean
    fun rideRequestEventsTopic() = NewTopic(RIDE_REQUEST_EVENTS, 3, 3)

    @Bean
    fun carLocations() = NewTopic("car_locations", 12, 3).configs(mapOf(TopicConfig.CLEANUP_POLICY_CONFIG to "compact"))
}
