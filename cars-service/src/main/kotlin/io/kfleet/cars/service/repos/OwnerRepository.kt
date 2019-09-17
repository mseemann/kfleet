package io.kfleet.cars.service.repos

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Output
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

interface IOwnerRepository {
    fun submitCreateOwnerCommand(ownerId: String, ownerName: String): Mono<Boolean>
    fun findOwnerByid(ownerId: String): Mono<Owner>
}

interface OwnersBindings {

    companion object {
        const val OWNER_COMMANDS = "owner_commands_out"
    }

    @Output(OWNER_COMMANDS)
    fun ownersCommands(): MessageChannel

}

@Repository
@EnableBinding(OwnersBindings::class)
class OwnerRepository(@Autowired @Output(OwnersBindings.OWNER_COMMANDS) val outputOwnerCommands: MessageChannel) : IOwnerRepository {


    override fun submitCreateOwnerCommand(ownerId: String, ownerName: String): Mono<Boolean> {
        val command = CreateOwnerCommand(id = ownerId, name = ownerName)

        val msg = MessageBuilder
                .withPayload(command)
                .setHeader(KafkaHeaders.MESSAGE_KEY, command.id)
                .setHeader("kfleet.type", CreateOwnerCommand::class.java.name)
                .build()

        return Mono.just(
                try {
                    // this works because cloud stream is configured sync for this topic
                    outputOwnerCommands.send(msg)
                } catch (e: RuntimeException) {
                    logger.warn(e) { e }
                    false
                }
        )
    }

    override fun findOwnerByid(ownerId: String): Mono<Owner> {
        return Mono.just(Owner(id = ownerId, name = "hallo", cars = listOf()))
    }
}
