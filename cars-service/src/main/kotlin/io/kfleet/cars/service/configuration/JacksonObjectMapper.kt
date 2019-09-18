package io.kfleet.cars.service.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.kfleet.cars.service.domain.Owner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


interface MixIn {
    @JsonIgnore
    fun getSchema(): org.apache.avro.Schema

    @JsonIgnore
    fun getSpecificData(): org.apache.avro.specific.SpecificData
}

@Configuration
class JacksonObjectMapper {

    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.addMixIn(Owner::class.java, MixIn::class.java)
        return mapper
    }
}
