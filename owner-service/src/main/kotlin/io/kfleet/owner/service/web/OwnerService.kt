package io.kfleet.owner.service.web

import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import io.kfleet.owner.service.domain.CarModel
import io.kfleet.owner.service.repos.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.Duration


data class NewCar(val model: CarModel)

@Component
class OwnerService(
        private val ownerRepository: OwnerRepository,
        private val commandsResponseRepository: CommandsResponseRepository) {

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
                .flatMap { findCommand(it.getCommandId()) }
                .flatMap { mapCommandResponse(it) { ownerById(it.getRessourceId(), HttpStatus.CREATED) } }
                .onErrorResume(IllegalArgumentException::class.java) { e -> toServerResponse(e) }
    }

    // The update ownerName is idempotent - so PUT is used. There is no optimistic locking
    // the owner is changed in any case. The last received update command "winns" the game.
    fun updateOwnersName(request: ServerRequest): Mono<ServerResponse> {
        val ownerId = request.pathVariable("ownerId")
        val ownerName = request.pathVariable("ownerName")

        return Mono.just(UpdateOwnerParams(ownerId.trim(), ownerName.trim()))
                .flatMap { validate(it) }
                .flatMap { ownerRepository.submitUpdateOwnerNameCommand(it) }
                .delayElement(Duration.ofMillis(200))
                .flatMap { findCommand(it.getCommandId()) }
                .flatMap { mapCommandResponse(it) { ownerById(it.getRessourceId()) } }
                .onErrorResume(IllegalArgumentException::class.java) { e -> toServerResponse(e) }
    }

    fun deleteOwner(request: ServerRequest): Mono<ServerResponse> {
        val ownerId = request.pathVariable("ownerId")
        return Mono.just(DeleteOwnerParams(ownerId.trim()))
                .flatMap { validate(it) }
                .flatMap { ownerRepository.submitDeleteOwnerCommand(it) }
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


    fun ownerById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")
        return ownerById(id).onErrorResume { ServerResponse.notFound().build() }
    }

    private fun ownerById(id: String, defaultStatus: HttpStatus = HttpStatus.OK): Mono<ServerResponse> {
        return ownerRepository
                .findById(id)
                .customRetry()
                .flatMap {
                    ServerResponse
                            .status(defaultStatus)
                            .body(BodyInserters.fromObject(it))
                }
    }

    fun registerACar(request: ServerRequest): Mono<ServerResponse> {
        val ownerId = request.pathVariable("ownerId")
        val newCar = request.bodyToMono(NewCar::class.java)
        println(ownerId)
        println(newCar)
        return ServerResponse.badRequest().build()
    }

    fun deregisterACar(request: ServerRequest): Mono<ServerResponse> {
        val ownerId = request.pathVariable("ownerId")
        val carId = request.pathVariable("carId")
        println(ownerId)
        println(carId)
        return ServerResponse.badRequest().build()
    }
}
