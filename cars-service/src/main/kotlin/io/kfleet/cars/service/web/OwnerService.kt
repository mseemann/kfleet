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
import java.time.Duration

private val log = KotlinLogging.logger {}


@RestController
@RequestMapping("/owners")
class OwnerService(
        @Autowired val ownerRepository: IOwnerRepository,
        @Autowired val commandsResponseRepository: ICommandsResponseRepositroy) {


    @PostMapping("/{ownerId}/{ownerName}")
    fun createOwner(
            @PathVariable("ownerId") ownerId: String,
            @PathVariable("ownerName") ownerName: String): Mono<ResponseEntity<out Any>> {

        return Mono.just(CreateOwnerParams(ownerId = ownerId.trim(), ownerName = ownerName.trim()))
                .flatMap { validate(it) }
                .flatMap { ownerRepository.submitCreateOwnerCommand(it) }
                .delayElement(Duration.ofMillis(100))
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
                                .findById(ownerId)
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

    }

    @GetMapping("/{id}")
    fun ownerById(@PathVariable("id") id: String): Mono<ResponseEntity<Owner>> = ownerRepository
            .findById(id)
            .customRetry()
            .map { ResponseEntity(it, HttpStatus.OK) }
            .onErrorResume { Mono.just(ResponseEntity(HttpStatus.NOT_FOUND)) }

}
