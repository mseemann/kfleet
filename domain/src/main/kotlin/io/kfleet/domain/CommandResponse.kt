package io.kfleet.domain

import io.kfleet.commands.CommandResponse
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue


inline fun commandResponse(buildCommandResponse: CommandResponse.Builder.() -> Unit): CommandResponse {
    val builder = CommandResponse.newBuilder()
    builder.buildCommandResponse()
    return builder.build()
}

fun CommandResponse.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getCommandId(), this)
}
