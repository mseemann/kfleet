package io.kfleet.riderequestdispatcher.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


const val RIDE_REQUEST_EVENTS = "ride_request_events"

@Configuration
class Topics {


    @Bean
    fun rideRequestEventsTopic() = NewTopic(RIDE_REQUEST_EVENTS, 3, 3)

    @Bean
    fun carLocations(): NewTopic = NewTopic("car_locations", 12, 3).configs(mapOf(TopicConfig.CLEANUP_POLICY_CONFIG to "compact"))
}
