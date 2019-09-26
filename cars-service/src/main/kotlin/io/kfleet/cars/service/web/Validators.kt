package io.kfleet.cars.service.web

import io.kfleet.cars.service.repos.CreateOwnerParams
import reactor.core.publisher.Mono

fun validate(newOwnerParams: CreateOwnerParams): Mono<CreateOwnerParams> {

    if (newOwnerParams.ownerId == "") return Mono.error(IllegalArgumentException("ownerId invalid"))
    if (newOwnerParams.ownerName == "") return Mono.error(IllegalArgumentException("ownerName invalid"))

    return Mono.just(newOwnerParams)
}
