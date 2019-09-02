package io.kfleet.dispatcher

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Configuration {

//    @Bean
//    fun objectMapper(): ObjectMapper {
//        val mapper = ObjectMapper()
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.registerModule(KotlinModule())
//        return mapper
//    }
}
