package io.kfleet.cars.service.processors

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.OwnerCommand
import io.kfleet.cars.service.events.OwnerCreatedEvent
import io.kfleet.cars.service.events.OwnerEvent
import mu.KotlinLogging
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Predicate
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener


private val logger = KotlinLogging.logger {}


interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_EVENTS = "owner_events"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, OwnerCommand>

}


@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor {


    @StreamListener(OwnerCommandsProcessorBinding.OWNER_COMMANDS)
    fun processCommands(commandStream: KStream<String, OwnerCommand>) {

        val stream = commandStream
                .peek { key, value -> println("cool: $key -> $value -> ${value.javaClass}") }


        val branches: Array<out KStream<String, OwnerCommand>> = stream
                .branch(
                        Predicate<String, OwnerCommand?> { key, value -> value?.getCommand() is CreateOwnerCommand },
                        Predicate<String, OwnerCommand?> { key, value -> true }
                )

        val createOwnerCommands = branches[0]
        val unknownOwnerCommands = branches[1]

        createOwnerCommands
                .map { k, ownerCreatedCommand ->
                    val name = (ownerCreatedCommand.getCommand() as CreateOwnerCommand).getName()
                    KeyValue(k, OwnerEvent(k, OwnerCreatedEvent(name)))
                }
                .to("owner_events")

        unknownOwnerCommands.to("unknown_commands")

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



