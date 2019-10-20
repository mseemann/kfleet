package io.kfleet.owner.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


// TopicNames
const val OWNER_COMMANDS = "owner_commands"
const val OWNER_EVENTS = "owner_events"
const val CAR_EVENTS = "car_events"
const val DLQ = "dlq"


// TopicBindingNames
const val OWNER_COMMANDS_IN = "owner_commands_in"
const val OWNER_COMMANDS_OUT = "owner_commands_out"
const val OWNER_EVENTS_IN = "owner_events_in"


// StoreNames
const val OWNER_COMMANDS_RESPONSE_STORE = "owner_commands_response_store"
const val OWNER_RW_STORE = "owners_store"


@Configuration
class Topics {

    @Bean
    fun ownerCommandsTopic() = NewTopic(OWNER_COMMANDS, 3, 3)

    @Bean
    fun dlqTopic() = NewTopic(DLQ, 3, 3)

    @Bean
    fun ownerEventsTopic() = NewTopic(OWNER_EVENTS, 3, 3)

    @Bean
    fun carEventsTopic() = NewTopic(CAR_EVENTS, 3, 3)
}
