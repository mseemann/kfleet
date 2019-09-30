package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.repos.CarsLocalRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers


const val CARS_RPC = "cars-rpc"

@RestController
@RequestMapping("/$CARS_RPC")
class CarsLocalRpcService(@Autowired private val carsRepository: CarsLocalRepository) {

    @GetMapping()
    fun cars(): Flux<Car> = carsRepository
            .findAllCarsLocal()
            .subscribeOn(Schedulers.elastic())

    @GetMapping("/{id}")
    fun carById(@PathVariable("id") id: String): Mono<Car> = carsRepository
            .findByIdLocal(id)
            .subscribeOn(Schedulers.elastic())

    @GetMapping("/stats")
    fun carsStateCount(): Mono<Map<String, Long>> = carsRepository
            .getLocalCarsStateCounts()
            .subscribeOn(Schedulers.elastic())
}
