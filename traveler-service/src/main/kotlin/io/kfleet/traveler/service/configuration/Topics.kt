package io.kfleet.traveler.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


// TopicNames
const val TRAVELER_COMMANDS = "traveler_commands"
const val TRAVELER_EVENTS = "traveler_events"
const val DLQ = "dlq"


// TopicBindingNames
const val TRAVELER_COMMANDS_IN = "traveler_commands_in"
const val TRAVELER_COMMANDS_OUT = "traveler_commands_out"


// StoreNames
const val TRAVELER_COMMANDS_RESPONSE_STORE = "traveler_commands_response_store"
const val TRAVELER_RW_STORE = "traveler_store"


@Configuration
class Topics {

    @Bean
    fun travelerCommandsTopic() = NewTopic(TRAVELER_COMMANDS, 3, 3)

    @Bean
    fun dlqTopic() = NewTopic(DLQ, 3, 3)

    @Bean
    fun travelerEventsTopic() = NewTopic(TRAVELER_EVENTS, 3, 3)


}
