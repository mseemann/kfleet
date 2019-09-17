package io.kfleet.cars.service.web

import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.repos.IOwnerRepository
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.time.Duration

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/owners")
class OwnerService(@Autowired val ownerRepository: IOwnerRepository) {

    // create owner
    // create a create owner command - with id
    // wait for en event for the id - the event is either created or not created
    // because the id is the key, the processor that is responsible
    // for the creation can check wether the key exists or not
    @PostMapping("/{ownerId}/{ownerName}")
    fun createOwner(
            @PathVariable("ownerId") ownerId: String?,
            @PathVariable("ownerName") ownerName: String?): Mono<ResponseEntity<Owner>> {

        if (ownerId == null || ownerId == "") return Mono.just(ResponseEntity(HttpStatus.BAD_REQUEST))
        if (ownerName == null || ownerName == "") return Mono.just(ResponseEntity(HttpStatus.BAD_REQUEST))

        return ownerRepository.submitCreateOwnerCommand(ownerId, ownerName).flatMap {
            if (it) ownerById(ownerId)
                    .flatMap { Mono.just(ResponseEntity(it.body, HttpStatus.CREATED)) }
            else Mono.just(ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR))
        }
    }


    // get owner
    @GetMapping("/{id}")
    fun ownerById(@PathVariable("id") id: String): Mono<ResponseEntity<Owner>> = ownerRepository
            .findOwnerByid(id)
            .retryBackoff(5, Duration.ofSeconds(1), Duration.ofSeconds(3))
            .map { ResponseEntity(it, HttpStatus.OK) }
            .onErrorResume { Mono.just(ResponseEntity(HttpStatus.NOT_FOUND)) }

}
