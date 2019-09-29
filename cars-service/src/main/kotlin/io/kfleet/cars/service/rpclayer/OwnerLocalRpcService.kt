package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.repos.IOwnerLocalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


const val OWNER_RPC = "owner-rpc"

@RestController
@RequestMapping("/$OWNER_RPC")
class OwnerLocalRpcService(@Autowired val ownerRepository: IOwnerLocalRepository) {

    @GetMapping("/{ownerId}")
    fun ownerById(@PathVariable("ownerId") ownerId: String) = ownerRepository
            .findByIdLocal(ownerId)


}
