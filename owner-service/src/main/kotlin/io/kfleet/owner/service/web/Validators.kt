package io.kfleet.owner.service.web

import io.kfleet.owner.service.repos.*
import reactor.core.publisher.Mono

private fun <T : OwnerParams> validateOwnerId(ownerParams: T): Mono<T> =
        if (ownerParams.ownerId == "") Mono.error(IllegalArgumentException("ownerId invalid")) else Mono.just(ownerParams)


fun validate(newOwnerParams: CreateOwnerParams): Mono<CreateOwnerParams> {

    if (newOwnerParams.ownerName == "") return Mono.error(IllegalArgumentException("ownerName invalid"))

    return Mono.just(newOwnerParams).flatMap { validateOwnerId(it) }
}

fun validate(updateOwnerParams: UpdateOwnerParams): Mono<UpdateOwnerParams> {
    if (updateOwnerParams.ownerName == "") return Mono.error(IllegalArgumentException("ownerName invalid"))

    return Mono.just(updateOwnerParams).flatMap { validateOwnerId(it) }
}

fun validate(deleteOwnerParams: DeleteOwnerParams): Mono<DeleteOwnerParams> {
    return Mono.just(deleteOwnerParams).flatMap { validateOwnerId(it) }
}


fun validate(registerCarParams: RegisterCarParams): Mono<RegisterCarParams> {
    return Mono.just(registerCarParams).flatMap { validateOwnerId(it) }
}

fun validate(deregisterCarParams: DeregisterCarParams): Mono<DeregisterCarParams> {

    if (deregisterCarParams.carId == "") return Mono.error(IllegalArgumentException("carId invalid"))

    return Mono.just(deregisterCarParams).flatMap { validateOwnerId(it) }
}
