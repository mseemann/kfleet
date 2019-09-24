package io.kfleet.cars.service.configuration

import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.streams.errors.DeserializationExceptionHandler
import org.apache.kafka.streams.errors.DeserializationExceptionHandler.DeserializationHandlerResponse
import org.apache.kafka.streams.processor.ProcessorContext


private val log = KotlinLogging.logger {}

class CustomDeserializationExceptionHandler : DeserializationExceptionHandler {

    override fun handle(context: ProcessorContext?, record: ConsumerRecord<ByteArray, ByteArray>?, exception: Exception?): DeserializationHandlerResponse {
        log.warn("${record} ${exception}")
        return DeserializationHandlerResponse.CONTINUE
    }

    override fun configure(configs: MutableMap<String, *>?) {
        log.info("${configs}")
    }
}
