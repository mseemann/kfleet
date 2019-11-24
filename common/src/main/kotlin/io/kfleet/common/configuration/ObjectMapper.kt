package io.kfleet.common.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.avro.specific.SpecificRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper().apply {
        addMixIn(SpecificRecord::class.java, MixInIgnoreAvroSchemaProperties::class.java)
    }
}
