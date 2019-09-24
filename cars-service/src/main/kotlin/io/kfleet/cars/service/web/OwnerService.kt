package io.kfleet.cars.service.web

import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.repos.IOwnerRepository
import io.kfleet.commands.CommandStatus
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import retryKfleet
import java.time.Duration

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/owners")
class OwnerService(@Autowired val ownerRepository: IOwnerRepository) {


    @PostMapping("/{ownerId}/{ownerName}")
    fun createOwner(
            @PathVariable("ownerId") ownerId: String?,
            @PathVariable("ownerName") ownerName: String?): Mono<ResponseEntity<out Any>> {

        if (ownerId == null || ownerId == "") return Mono.just(ResponseEntity(HttpStatus.BAD_REQUEST))
        if (ownerName == null || ownerName == "") return Mono.just(ResponseEntity(HttpStatus.BAD_REQUEST))

        return ownerRepository.submitCreateOwnerCommand(ownerId, ownerName)
                // a delay is required (or at least a retry) because the submitCreateOwnerCommand
                // results in an async operation by the stream  processor.
                // the delay time depends on the system performance
                .delayElement(Duration.ofMillis(100))
                .flatMap { command ->
                    log.debug { "submitted command: $command" }
                    ownerRepository
                            .findCommandResponse(command.getCommandId())
                            .retryKfleet()
                }
                .flatMap { commandResponse ->
                    log.debug { "commandResponse: $commandResponse" }
                    if (commandResponse.getStatus() == CommandStatus.REJECTED) {
                        Mono.just(ResponseEntity(commandResponse.getReason(), HttpStatus.BAD_REQUEST))
                    } else {
                        ownerRepository
                                .findOwnerByid(ownerId)
                                .retryKfleet()
                                .flatMap {
                                    log.debug { "response form find ownerbyid: $it" }
                                    Mono.just(ResponseEntity(it, HttpStatus.CREATED))
                                }
                    }
                }
    }

    @GetMapping("/{id}")
    fun ownerById(@PathVariable("id") id: String): Mono<ResponseEntity<Owner>> = ownerRepository
            .findOwnerByid(id)
            .retryKfleet()
            .map { ResponseEntity(it, HttpStatus.OK) }
            .onErrorResume { Mono.just(ResponseEntity(HttpStatus.NOT_FOUND)) }

}
