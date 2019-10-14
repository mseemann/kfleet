package io.kfleet.common.configuration

import com.fasterxml.jackson.annotation.JsonIgnore

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
