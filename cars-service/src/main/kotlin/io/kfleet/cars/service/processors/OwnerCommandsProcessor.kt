package io.kfleet.cars.service.processors

import com.fasterxml.jackson.databind.ObjectMapper
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.OwnerCommand
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.apache.kafka.streams.kstream.Produced
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.SendToDlqAndContinue
import org.springframework.kafka.support.serializer.JsonSerde


private val logger = KotlinLogging.logger {}

interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_EVENTS = "owner_events"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, OwnerCommand>

}

private var testCounter = 0

@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor(@Autowired val mapper: ObjectMapper) {

    @Autowired
    private val dlqHandler: SendToDlqAndContinue? = null

    @StreamListener(OwnerCommandsProcessorBinding.OWNER_COMMANDS)
    fun processCommands(commandStream: KStream<String, OwnerCommand>) {

        val stream = commandStream
//                .transformValues(object : ValueTransformerSupplier<String, OwnerCommand?> {
//                    override fun get(): ValueTransformer<String, OwnerCommand?> {
//                        return object : ValueTransformer<String, OwnerCommand?> {
//                            lateinit var context: ProcessorContext
//                            override fun init(context: ProcessorContext) {
//                                this.context = context
//                            }
//
//                            override fun transform(value: String?): OwnerCommand? {
//                                context.headers().forEach { println("${it.key()} -> ${String(it.value())}") }
//                                println("$value")
//                                val typeName = context.headers()?.headers("kfleet.type")?.firstOrNull()
//                                typeName?.let {
//                                    val typeNameString = String(typeName.value())
//                                    val className: String = mapper.readValue(typeNameString)
//                                    println(className)
//                                    val command = mapper.readValue(value!!, Class.forName(className)) as OwnerCommand
//                                    println(command)
//                                    return command
//                                }
//                                return null
//                            }
//
//                            override fun close() {}
//                        }
//                    }
//
//                })
                .peek { key, value -> println("cool: $key -> $value") }

        val ownerEvents = stream.filter { _, _ -> testCounter++; testCounter % 2 == 0 }
        ownerEvents.to("test_output_stream", Produced.with(Serdes.String(), JsonSerde<OwnerCommand>()))


        val branches: Array<out KStream<String, OwnerCommand?>> = stream
                .mapValues { key, value ->
                    println("map $key -> $value")
                    // command 2 event
                    value
                }.branch(
                        Predicate<String, OwnerCommand?> { key, value ->
                            println("check1 $key $value")
                            value is CreateOwnerCommand
                        },
                        Predicate<String, OwnerCommand?> { key, value ->
                            println("check2 $key $value")
                            true
                        }
                )
        // error stream -> forward to dlq and windowed events stream
        //branches[0].peek { key, value -> println("cool1: $key -> $value") }
        // event stream -> forward to owner_events;  materialue stream to owner topic and windowed events stream
        //branches[1].peek { key, value -> println("cool2: $key -> $value") }
//                .through("owner_events")
//                // map to owner - e.g. create or read and modifiy
//                .through("owner")
//                .groupByKey()
//                .windowedBy(TimeWindows.of(Duration.ofMinutes(60).toMillis()))
//                .aggregate(EventsByOwnerId::new, x, Materialized.`as`("latest_events_by_command_id"))

        // group all events by owner id and store them in a list withijn a timewindow
        // every event must contain the source command id - so we can find the events that a command
        // created - for example find the reason why a owner could not be created


    }
}



