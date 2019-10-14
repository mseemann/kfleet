package io.kfleet.owner.service.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import io.kfleet.common.configuration.MixInIgnoreAvroSchemaProperties
import org.apache.avro.specific.SpecificRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class JacksonObjectMapper {

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.addMixIn(SpecificRecord::class.java, MixInIgnoreAvroSchemaProperties::class.java)
        return mapper
    }
}
