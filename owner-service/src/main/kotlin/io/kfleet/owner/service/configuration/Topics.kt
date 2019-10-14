package io.kfleet.owner.service.configuration

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class Topics {
    
    @Bean
    fun ownerCommandsTopic() = NewTopic("owner_commands", 3, 3)

    @Bean
    fun unknownCommandsTopic() = NewTopic("unknown_owner_commands", 3, 3)

}
