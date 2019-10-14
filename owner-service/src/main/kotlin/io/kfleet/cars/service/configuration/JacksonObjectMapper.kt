package io.kfleet.cars.service.configuration

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.avro.specific.SpecificRecord
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * The Domain Objects are AVRO-Objects and contain some properties that are specific
 * to avro - for example the schema. These properties are not needed in serialized
 * Domain-Objects or throwing exception. This mixin markes these properties as
 * ignored by the json serializer.
 */
interface MixInIgnoreAvroSchemaProperties {
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
        mapper.addMixIn(SpecificRecord::class.java, MixInIgnoreAvroSchemaProperties::class.java)
        return mapper
    }
}
