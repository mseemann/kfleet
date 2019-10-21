package io.kfleet.traveler.service.web


import io.kfleet.traveler.service.repos.DeleteTravelerParams
import io.kfleet.traveler.service.repos.TravelerParams
import reactor.core.publisher.Mono

private fun <T : TravelerParams> validateTravelerId(travelerParams: T): Mono<T> =
        if (travelerParams.travelerId == "") Mono.error(IllegalArgumentException("travelerId invalid")) else Mono.just(travelerParams)

private fun validateTravelerId(newTraveler: NewTraveler): Mono<NewTraveler> =
        if (newTraveler.id == "") Mono.error(IllegalArgumentException("travelerId invalid")) else Mono.just(newTraveler)


private fun validateTravelerName(newTraveler: NewTraveler): Mono<NewTraveler> =
        if (newTraveler.name == "") Mono.error(IllegalArgumentException("travelerName invalid")) else Mono.just(newTraveler)

private fun validateTravelerEmail(newTraveler: NewTraveler): Mono<NewTraveler> =
        if (newTraveler.email == "") Mono.error(IllegalArgumentException("travelerEmail invalid")) else Mono.just(newTraveler)


fun validateNewTraveler(newTravelerParams: NewTraveler): Mono<NewTraveler> =
        Mono.just(newTravelerParams)
                .flatMap { validateTravelerId(it) }
                .flatMap { validateTravelerName(it) }
                .flatMap { validateTravelerEmail(it) }


fun validate(deleteTravelerParams: DeleteTravelerParams): Mono<DeleteTravelerParams> =
        Mono.just(deleteTravelerParams)
                .flatMap { validateTravelerId(it) }


