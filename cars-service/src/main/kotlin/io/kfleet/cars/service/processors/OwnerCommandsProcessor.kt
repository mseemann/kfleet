package io.kfleet.cars.service.processors

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.OwnerCommand
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.kstream.ValueTransformer
import org.apache.kafka.streams.kstream.ValueTransformerSupplier
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.ProcessorSupplier
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.kafka.support.serializer.JsonSerde

private val logger = KotlinLogging.logger {}

interface OwnerCommandsProcessorBinding {

    companion object {
        const val OWNER_COMMANDS = "owner_commands"
        const val OWNER_EVENTS = "owner_events"
    }

    @Input(OWNER_COMMANDS)
    fun inputOwnerCommands(): KStream<String, String>

}

@EnableBinding(OwnerCommandsProcessorBinding::class)
class OwnerCommandsProcessor {

    @StreamListener(OwnerCommandsProcessorBinding.OWNER_COMMANDS)
    fun processCommands(
            @Input(OwnerCommandsProcessorBinding.OWNER_COMMANDS) commandStream: KStream<String, String>) {

        commandStream
                .transformValues(object : ValueTransformerSupplier<String, OwnerCommand> {
                    override fun get(): ValueTransformer<String, OwnerCommand> {
                        return object : ValueTransformer<String, OwnerCommand> {
                            lateinit var ctx: ProcessorContext
                            override fun init(context: ProcessorContext?) {
                                ctx = context!!
                            }

                            override fun transform(value: String?): OwnerCommand {
                                ctx.headers().forEach { println("${it.key()} -> ${String(it.value())}") }
                                println("$value")
                                ctx.headers().add("test", "test-value".toByteArray())
                                return CreateOwnerCommand(id = "test", name = "test name")
                            }

                            override fun close() {
                                println("close called")
                            }
                        }
                    }

                })
                .peek { key, value -> println("cool: $key -> $value") }
                .to("test_output_stream", Produced.with(Serdes.String(), JsonSerde<OwnerCommand>()))

        commandStream.process(object : ProcessorSupplier<String, String> {
            override fun get(): Processor<String, String> {
                return object : Processor<String, String> {

                    lateinit var ctx: ProcessorContext

                    override fun init(context: ProcessorContext) {
                        ctx = context
                        println("$ctx")
                    }

                    override fun process(key: String?, value: String?) {
                        ctx.headers().forEach { println("im Processor ${it.key()} -> ${String(it.value())}") }
                        println("value im processor $value")

                    }

                    override fun close() {
                        println("close called")
                    }

                }
            }
        })

    }
}



