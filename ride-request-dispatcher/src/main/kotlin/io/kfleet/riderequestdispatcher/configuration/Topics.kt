package io.kfleet.car.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


// TopicBindingNames
const val CAR_EVENTS_IN = "car_events_in"


@Configuration
class Topics {


    @Bean
    fun carLocations() = NewTopic("car_locations", 12, 3).configs(mapOf(TopicConfig.CLEANUP_POLICY_CONFIG to "compact"))
}
