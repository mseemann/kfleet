package io.kfleet.domain

import io.kfleet.commands.CommandResponse
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.streams.KeyValue


fun commandResponse(buildCommandResponse: CommandResponse.Builder.() -> Unit): CommandResponse =
        CommandResponse.newBuilder().apply { buildCommandResponse() }.build()


fun CommandResponse.asKeyValue(): KeyValue<String, SpecificRecord?> {
    return KeyValue(this.getCommandId(), this)
}
