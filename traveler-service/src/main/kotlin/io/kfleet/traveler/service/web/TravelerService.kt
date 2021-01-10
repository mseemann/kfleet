package io.kfleet.traveler.service.web

import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import io.kfleet.traveler.service.commands.GeoPositionCarRequest
import io.kfleet.traveler.service.domain.geoPositionCarRequest
import io.kfleet.traveler.service.repos.CommandsResponseRepository
import io.kfleet.traveler.service.repos.TravelerRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

interface TravelerParams {
    val travelerId: String
}

data class DeleteTravelerParams(override val travelerId: String) : TravelerParams
data class NewTraveler(override val travelerId: String, val name: String, val email: String) : TravelerParams
data class CarRequestGeoPosition(val lat: Double, val lng: Double)
data class CarRequest(
    val id: String,
    override val travelerId: String,
    val from: CarRequestGeoPosition,
    val to: CarRequestGeoPosition,
    val requestTime: Date
) : TravelerParams

fun CarRequestGeoPosition.toGeoPositionCarRequest(): GeoPositionCarRequest {
    return geoPositionCarRequest {
        lat = this.lat
        lng = this.lng
    }
}


@Component
class TravelerService(
    private val travelerRepository: TravelerRepository,
    private val commandsResponseRepository: CommandsResponseRepository
) {

    // The client is responsible to create a globally unique id (for example a uuid).
    // Post is used to state that this operation is not idempotent. If something
    // goes wrong the client can query for the travelerId later and check if the traveler
    // is created or not. In most cases this call will return the created traveler.
    fun createTraveler(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono<NewTraveler>()
            .flatMap { validateNewTraveler(it) }
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


    private fun mapCommandResponse(
        cResponse: CommandResponse,
        mapResult: () -> Mono<ServerResponse>
    ): Mono<ServerResponse> =
        if (cResponse.getStatus() == CommandStatus.REJECTED) {
            ServerResponse.badRequest().body(BodyInserters.fromObject(cResponse.getReason()))
        } else {
            mapResult()
        }


    private fun toServerResponse(e: IllegalArgumentException) =
        ServerResponse.badRequest().body(BodyInserters.fromObject(e.message ?: "unknown"))


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

    fun requestACar(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono<CarRequest>()
            .flatMap { validateCarRequest(it) }
            .flatMap { travelerRepository.submitCarRequestTravelerCommand(it) }
            .delayElement(Duration.ofMillis(200))
            .flatMap { findCommand(it.getCommandId()) }
            .flatMap { mapCommandResponse(it) { ServerResponse.noContent().build() } }
            .onErrorResume(IllegalArgumentException::class.java) { e -> toServerResponse(e) }
    }
}

