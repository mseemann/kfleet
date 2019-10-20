package io.kfleet.traveler.service.web

import io.kfleet.traveler.service.repos.CreateTravelerParams
import io.kfleet.traveler.service.repos.DeleteTravelerParams
import io.kfleet.traveler.service.repos.TravelerParams
import reactor.core.publisher.Mono

private fun <T : TravelerParams> validateTravelerId(travelerParams: T): Mono<T> =
        if (travelerParams.travelerId == "") Mono.error(IllegalArgumentException("travelerId invalid")) else Mono.just(travelerParams)

private fun validateTravelerName(createTravelerParams: CreateTravelerParams): Mono<CreateTravelerParams> =
        if (createTravelerParams.travelerName == "") Mono.error(IllegalArgumentException("travelerName invalid")) else Mono.just(createTravelerParams)

private fun validateTravelerEmail(createTravelerParams: CreateTravelerParams): Mono<CreateTravelerParams> =
        if (createTravelerParams.travelerEmail == "") Mono.error(IllegalArgumentException("travelerEmail invalid")) else Mono.just(createTravelerParams)


fun validate(newTravelerParams: CreateTravelerParams): Mono<CreateTravelerParams> =
        Mono.just(newTravelerParams)
                .flatMap { validateTravelerId(it) }
                .flatMap { validateTravelerName(it) }
                .flatMap { validateTravelerEmail(it) }


fun validate(deleteTravelerParams: DeleteTravelerParams): Mono<DeleteTravelerParams> =
        Mono.just(deleteTravelerParams)
                .flatMap { validateTravelerId(it) }


