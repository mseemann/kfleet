package io.kfleet.cars.service.web

import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.repos.CreateOwnerParams
import io.kfleet.cars.service.repos.ICommandsResponseRepositroy
import io.kfleet.cars.service.repos.IOwnerRepository
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration

private val log = KotlinLogging.logger {}


@RestController
@RequestMapping("/owners")
class OwnerService(
        @Autowired val ownerRepository: IOwnerRepository,
        @Autowired val commandsResponseRepository: ICommandsResponseRepositroy) {


    // The client is responsible to create a globally unique id (for example a uuid).
    // Post is used to state that this operation is not idempotent. If something
    // goes wrong the client can query for the owneid later and check if the owner
    // is created or not. In most cases this call will return the created owner.
    @PostMapping("/{ownerId}/{ownerName}")
    fun createOwner(
            @PathVariable("ownerId") ownerId: String,
            @PathVariable("ownerName") ownerName: String): Mono<ResponseEntity<out Any>> {

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
                        Mono.just(ResponseEntity(it.getReason(), HttpStatus.BAD_REQUEST))
                    } else {
                        ownerRepository
                                .findById(it.getRessourceId())
                                .customRetry()
                                .flatMap {
                                    Mono.just(ResponseEntity(it, HttpStatus.CREATED))
                                }
                    }
                }
                .onErrorResume(java.lang.IllegalArgumentException::class.java) { e ->
                    e.message?.let { Mono.just(ResponseEntity(it, HttpStatus.BAD_REQUEST)) }
                            ?: Mono.just(ResponseEntity("unknown", HttpStatus.BAD_REQUEST))
                }
                .subscribeOn(Schedulers.elastic())

    }

    // The update ownerName is idempotent - so PUT is used. There is no optimistic locking
    // the owner is cahnged in any case. The last received update command "winns" the game.
    @PutMapping("/{ownerId}/{ownerName}")
    fun updateOwnersName(@PathVariable ownerId: String, @PathVariable ownerName: String) {

    }

    @GetMapping("/{id}")
    fun ownerById(@PathVariable("id") id: String): Mono<ResponseEntity<Owner>> = ownerRepository
            .findById(id)
            .customRetry()
            .map { ResponseEntity(it, HttpStatus.OK) }
            .onErrorResume { Mono.just(ResponseEntity(HttpStatus.NOT_FOUND)) }
            .subscribeOn(Schedulers.elastic())

}
