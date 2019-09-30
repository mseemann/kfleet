package io.kfleet.cars.service.web

import io.kfleet.cars.service.repos.CommandsResponseRepository
import io.kfleet.cars.service.repos.CreateOwnerParams
import io.kfleet.cars.service.repos.OwnerRepository
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

private val log = KotlinLogging.logger {}

@Component
class OwnerService(
        @Autowired private val ownerRepository: OwnerRepository,
        @Autowired private val commandsResponseRepository: CommandsResponseRepository) {


    // The client is responsible to create a globally unique id (for example a uuid).
    // Post is used to state that this operation is not idempotent. If something
    // goes wrong the client can query for the ownerid late and check if the owner
    // is created or not. In most cases this call will return the created owner.
    fun createOwner(request: ServerRequest): Mono<ServerResponse> {
        val ownerId = request.pathVariable("ownerId")
        val ownerName = request.pathVariable("ownerName")

        return Mono.just(CreateOwnerParams(ownerId.trim(), ownerName.trim()))
                .flatMap { validate(it) }
                .flatMap { ownerRepository.submitCreateOwnerCommand(it) }
                .delayElement(Duration.ofMillis(200))
                .flatMap {
                    commandsResponseRepository
                            .findCommandResponse(it.getCommandId())
                            .customRetry()
                }
                .flatMap {
                    if (it.getStatus() == CommandStatus.REJECTED) {
                        ServerResponse.badRequest().body(BodyInserters.fromObject(it.getReason()))
                    } else {
                        ownerRepository
                                .findById(it.getRessourceId())
                                .customRetry()
                                .flatMap {
                                    ServerResponse
                                            .status(HttpStatus.CREATED)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .body(BodyInserters.fromObject(it))
                                }
                    }
                }
                .onErrorResume(java.lang.IllegalArgumentException::class.java) { e ->
                    ServerResponse.badRequest().body(BodyInserters.fromObject(e.message?.let { it } ?: "unknown"))
                }
                .subscribeOn(Schedulers.elastic())
    }

    // The update ownerName is idempotent - so PUT is used. There is no optimistic locking
    // the owner is cahnged in any case. The last received update command "winns" the game.
    fun updateOwnersName(request: ServerRequest): Mono<ServerResponse> {

        return ServerResponse.noContent().build()
    }

    fun ownerById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")

        return ownerRepository
                .findById(id)
                .customRetry()
                .flatMap {
                    ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromObject(it))
                }
                .onErrorResume { ServerResponse.notFound().build() }
                .subscribeOn(Schedulers.elastic())
    }
}
