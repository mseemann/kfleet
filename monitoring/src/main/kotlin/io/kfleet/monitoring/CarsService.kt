package io.kfleet.monitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Car
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class CarsService {

    @Autowired
    lateinit var carRepository: CarRepository

    val mapper = jacksonObjectMapper()


    @RequestMapping("/cars")
    fun cars(): List<Car> {
        return carRepository.allCarsStore().all().use { it.asSequence().map { kv -> mapper.readValue<Car>(kv.value) }.toList() }
    }

    @RequestMapping("/cars/{id}")
    fun car(@PathVariable("id") id: String): Car? {
        return carRepository.allCarsStore().get(id)?.let { mapper.readValue<Car>(it) }
    }

    @RequestMapping("/cars/stats")
    fun carsStats(): Map<String, Long> {
        return carRepository.allCarStateStore().all().use { it.asSequence().map { kv -> kv.key to kv.value }.toMap() }
    }
}
