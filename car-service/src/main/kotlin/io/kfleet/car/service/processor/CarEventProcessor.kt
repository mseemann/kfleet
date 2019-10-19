package io.kfleet.car.service.processor

import io.kfleet.car.service.configuration.TopicBindingNames
import mu.KotlinLogging
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.kstream.KStream
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener

private val log = KotlinLogging.logger {}

interface CarEventsProcessorBinding {

    @Input(TopicBindingNames.CAR_EVENTS_IN)
    fun inputCarEvents(): KStream<String, SpecificRecord>

}

@EnableBinding(CarEventsProcessorBinding::class)
class CarEventProcessor {

    @StreamListener
    fun processEvents(@Input(TopicBindingNames.CAR_EVENTS_IN) carEventStream: KStream<String, SpecificRecord>) {

        carEventStream.peek { key, value -> log.debug { "car event: $key -> $value" } }
    }

}
