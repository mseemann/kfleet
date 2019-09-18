package io.kfleet.cars.service.configuration

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.errors.DeserializationExceptionHandler
import org.apache.kafka.streams.errors.DeserializationExceptionHandler.DeserializationHandlerResponse
import org.apache.kafka.streams.processor.ProcessorContext

class CustomDeserializationExceptionHandler : DeserializationExceptionHandler {

    override fun handle(context: ProcessorContext?, record: ConsumerRecord<ByteArray, ByteArray>?, exception: Exception?): DeserializationHandlerResponse {
        println("${record} ${exception}")
        return DeserializationHandlerResponse.CONTINUE
    }

    override fun configure(configs: MutableMap<String, *>?) {
        println("${configs}")
    }
}
