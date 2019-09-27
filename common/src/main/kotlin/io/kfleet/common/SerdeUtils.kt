package io.kfleet.common

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord


inline fun <reified T : SpecificRecord> createSerdeWithAvroRegistry(endpoint: String): () -> SpecificAvroSerde<T> {
    return {
        val serdeConfig = mapOf(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to endpoint)
        val serde = SpecificAvroSerde<T>()
        serde.configure(serdeConfig, false)
        serde
    }
}
