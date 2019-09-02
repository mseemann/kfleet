package io.kfleet.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer


class JsonSerde<T>() : org.springframework.kafka.support.serializer.JsonSerde<T>() {


    init {

    }


    override fun deserializer(): Deserializer<T> {
        val mapper = jacksonObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(KotlinModule())

        //val json = """{"name":"Endgame","studio":"Marvel"}"""
        // val car: Car = mapper.readValue(json)

        return org.springframework.kafka.support.serializer.JsonDeserializer<T>(null, mapper)
    }

}
