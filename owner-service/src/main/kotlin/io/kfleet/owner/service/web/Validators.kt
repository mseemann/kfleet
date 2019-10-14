package io.kfleet.owner.service.web

import io.kfleet.owner.service.repos.CreateOwnerParams
import io.kfleet.owner.service.repos.DeleteOwnerParams
import io.kfleet.owner.service.repos.UpdateOwnerParams
import reactor.core.publisher.Mono

fun validate(newOwnerParams: CreateOwnerParams): Mono<CreateOwnerParams> {

    if (newOwnerParams.ownerId == "") return Mono.error(IllegalArgumentException("ownerId invalid"))
    if (newOwnerParams.ownerName == "") return Mono.error(IllegalArgumentException("ownerName invalid"))

    return Mono.just(newOwnerParams)
}

fun validate(updateOwnerParams: UpdateOwnerParams): Mono<UpdateOwnerParams> {

    if (updateOwnerParams.ownerId == "") return Mono.error(IllegalArgumentException("ownerId invalid"))
    if (updateOwnerParams.ownerName == "") return Mono.error(IllegalArgumentException("ownerName invalid"))

    return Mono.just(updateOwnerParams)
}

fun validate(deleteOwnerParams: DeleteOwnerParams): Mono<DeleteOwnerParams> {

    if (deleteOwnerParams.ownerId == "") return Mono.error(IllegalArgumentException("ownerId invalid"))

    return Mono.just(deleteOwnerParams)
}
