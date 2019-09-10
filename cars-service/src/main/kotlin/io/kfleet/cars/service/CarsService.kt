package io.kfleet.cars.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.cars.service.domain.Car
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/cars")
class CarsService {

    @Autowired
    lateinit var carRepository: CarsRepository

    @Autowired
    lateinit var mapper: ObjectMapper


    @RequestMapping("/")
    fun cars(): List<Car> {
        return carRepository.carsStore().all().use {
            it.asSequence().map { kv -> mapper.readValue<Car>(kv.value) }.toList()
        }
    }

    @RequestMapping("/{id}")
    fun car(@PathVariable("id") id: String): Car? {
        return carRepository.carsStore().get(id)?.let { mapper.readValue<Car>(it) }
    }

    @RequestMapping("/stats")
    fun carsStats(): Map<String, Long> {
        return carRepository.carStateStore().all().use {
            it.asSequence().map { kv -> kv.key to kv.value }.toMap()
        }
    }
}
