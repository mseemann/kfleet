package io.kfleet.monitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Traveler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/travelers")
class TravelersService {

    @Autowired
    lateinit var travelerRepository: TravelersRepository

    val mapper = jacksonObjectMapper()


    @RequestMapping("/")
    fun travelers(): List<Traveler> {
        return travelerRepository.travelersStore().all().use { it.asSequence().map { kv -> mapper.readValue<Traveler>(kv.value) }.toList() }
    }

    @RequestMapping("/{id}")
    fun traveler(@PathVariable("id") id: String): Traveler? {
        return travelerRepository.travelersStore().get(id)?.let { mapper.readValue<Traveler>(it) }
    }

    @RequestMapping("/stats")
    fun travelersStats(): Map<String, Long> {
        return travelerRepository.travelersStateStore().all().use { it.asSequence().map { kv -> kv.key to kv.value }.toMap() }
    }
}
