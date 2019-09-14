package io.kfleet.cars.service.rpclayer

import io.kfleet.cars.service.domain.Car
import io.kfleet.cars.service.repos.ICarsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/cars-local-only")
class CarsRpcService(@Autowired val carsRepository: ICarsRepository) {

    @GetMapping()
    fun cars(): Mono<List<Car>> = carsRepository.findAllCarsLocal()

    @GetMapping("/{id}")
    fun carById(@PathVariable("id") id: String): Mono<Car> = carsRepository.findByIdLocal(id)
}
