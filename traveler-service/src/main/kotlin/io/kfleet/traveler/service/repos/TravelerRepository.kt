package io.kfleet.traveler.service.repos

import io.kfleet.common.WebClientUtil
import io.kfleet.traveler.service.commands.CreateTravelerCommand
import io.kfleet.traveler.service.commands.DeleteTravelerCommand
import io.kfleet.traveler.service.configuration.TRAVELER_COMMANDS_OUT
import io.kfleet.traveler.service.configuration.TRAVELER_RW_STORE
import io.kfleet.traveler.service.domain.Traveler
import io.kfleet.traveler.service.domain.createTravelerCommand
import io.kfleet.traveler.service.domain.deleteTravelerCommand
import io.kfleet.traveler.service.rpclayer.RPC_TRAVELER
import io.kfleet.traveler.service.web.NewTraveler
import mu.KotlinLogging
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.*

interface TravelerParams {
    val travelerId: String
}

data class DeleteTravelerParams(override val travelerId: String) : TravelerParams


private val log = KotlinLogging.logger {}

interface TravelerBindings {

    @Output(TRAVELER_COMMANDS_OUT)
    fun travelerCommands(): MessageChannel

}

@Component
@EnableBinding(TravelerBindings::class)
class TravelerRepository(
        @Output(TRAVELER_COMMANDS_OUT) private val outputTravelerCommands: MessageChannel,
        private val interactiveQueryService: InteractiveQueryService,
        private val webClientUtil: WebClientUtil) {

    fun submitCreateTravelerCommand(createTravelerParams: NewTraveler): Mono<CreateTravelerCommand> {

        val travelerCommand = createTravelerCommand {
            commandId = UUID.randomUUID().toString()
            travelerId = createTravelerParams.id
            name = createTravelerParams.name
            email = createTravelerParams.email
        }

        val msg = MessageBuilder
                .withPayload(travelerCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, travelerCommand.getTravelerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputTravelerCommands.send(msg)) Mono.just(travelerCommand) else Mono.error(RuntimeException("CreateTravelerCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }


    fun submitDeleteTravelerCommand(deleteTravelerParams: DeleteTravelerParams): Mono<DeleteTravelerCommand> {
        val travelerCommand = deleteTravelerCommand {
            commandId = UUID.randomUUID().toString()
            travelerId = deleteTravelerParams.travelerId
        }

        val msg = MessageBuilder
                .withPayload(travelerCommand)
                .setHeader(KafkaHeaders.MESSAGE_KEY, travelerCommand.getTravelerId())
                .build()

        return try {
            // this works because cloud stream is configured as sync for this topic
            if (outputTravelerCommands.send(msg)) Mono.just(travelerCommand) else Mono.error(RuntimeException("DeleteTravelerCommand coud not be send."))
        } catch (e: RuntimeException) {
            Mono.error(e)
        }
    }

    fun findById(travelerId: String): Mono<Traveler> {
        val hostInfo = interactiveQueryService.getHostInfo(TRAVELER_RW_STORE, travelerId, StringSerializer())
        return webClientUtil.doGet(hostInfo, "$RPC_TRAVELER/$travelerId", Traveler::class.java)
    }

}
