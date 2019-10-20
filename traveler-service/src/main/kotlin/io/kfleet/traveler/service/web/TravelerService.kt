package io.kfleet.traveler.service.web

import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import io.kfleet.traveler.service.repos.CommandsResponseRepository
import io.kfleet.traveler.service.repos.CreateTravelerParams
import io.kfleet.traveler.service.repos.DeleteTravelerParams
import io.kfleet.traveler.service.repos.TravelerRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.Duration


@Component
class TravelerService(
        private val travelerRepository: TravelerRepository,
        private val commandsResponseRepository: CommandsResponseRepository) {

    // The client is responsible to create a globally unique id (for example a uuid).
    // Post is used to state that this operation is not idempotent. If something
    // goes wrong the client can query for the travelerid later and check if the traveler
    // is created or not. In most cases this call will return the created traveler.
    fun createTraveler(request: ServerRequest): Mono<ServerResponse> {
        val travelerId = request.pathVariable("travelerId")
        val travelerName = request.pathVariable("travelerName")
        val travelerEmail = request.pathVariable("travelerEmail")

        return Mono.just(CreateTravelerParams(
                travelerId = travelerId.trim(),
                travelerName = travelerName.trim(),
                travelerEmail = travelerEmail.trim()))
                .flatMap { validate(it) }
                .flatMap { travelerRepository.submitCreateTravelerCommand(it) }
                .delayElement(Duration.ofMillis(200))
                .flatMap { findCommand(it.getCommandId()) }
                .flatMap { mapCommandResponse(it) { travelerById(it.getRessourceId(), HttpStatus.CREATED) } }
                .onErrorResume(IllegalArgumentException::class.java) { e -> toServerResponse(e) }
    }

    fun deleteTraveler(request: ServerRequest): Mono<ServerResponse> {
        val travelerId = request.pathVariable("travelerId")
        return Mono.just(DeleteTravelerParams(travelerId.trim()))
                .flatMap { validate(it) }
                .flatMap { travelerRepository.submitDeleteTravelerCommand(it) }
                .delayElement(Duration.ofMillis(200))
                .flatMap { findCommand(it.getCommandId()) }
                .flatMap { mapCommandResponse(it) { ServerResponse.noContent().build() } }
                .onErrorResume(IllegalArgumentException::class.java) { e -> toServerResponse(e) }
    }

    private fun findCommand(commandId: String): Mono<CommandResponse> = commandsResponseRepository
            .findCommandResponse(commandId)
            .customRetry()


    private fun mapCommandResponse(cResponse: CommandResponse, mapResult: () -> Mono<ServerResponse>): Mono<ServerResponse> =
            if (cResponse.getStatus() == CommandStatus.REJECTED) {
                ServerResponse.badRequest().body(BodyInserters.fromObject(cResponse.getReason()))
            } else {
                mapResult()
            }


    private fun toServerResponse(e: IllegalArgumentException) =
            ServerResponse.badRequest().body(BodyInserters.fromObject(e.message?.let { it } ?: "unknown"))


    fun travelerById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")
        return travelerById(id).onErrorResume { ServerResponse.notFound().build() }
    }

    private fun travelerById(id: String, defaultStatus: HttpStatus = HttpStatus.OK): Mono<ServerResponse> {
        return travelerRepository
                .findById(id)
                .customRetry()
                .flatMap {
                    ServerResponse
                            .status(defaultStatus)
                            .body(BodyInserters.fromObject(it))
                }
    }

}

