package io.kfleet.cars.service.rpclayer


import io.kfleet.cars.service.repos.OwnerLocalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


const val OWNER_RPC = "owner-rpc"

@Component
class OwnerLocalRpcService(@Autowired private val ownerRepository: OwnerLocalRepository) {

    fun ownerById(request: ServerRequest): Mono<ServerResponse> {
        val ownerId = request.pathVariable("ownerId")
        return ownerRepository
                .findByIdLocal(ownerId)
                .flatMap { ServerResponse.ok().body(BodyInserters.fromObject(it)) }
                .onErrorResume { ServerResponse.notFound().build() }
                .subscribeOn(Schedulers.elastic())
    }


}
