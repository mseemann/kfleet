package io.kfleet.monitoring

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.domain.Traveler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class TravelerService {

    @Autowired
    lateinit var travelerRepository: TravelerRepository

    val mapper = jacksonObjectMapper()


    @RequestMapping("/travelers")
    fun travelers(): List<Traveler> {
        return travelerRepository.allTravelersStore().all().use { it.asSequence().map { kv -> mapper.readValue<Traveler>(kv.value) }.toList() }
    }

    @RequestMapping("/travelers/{id}")
    fun traveler(@PathVariable("id") id: String): Traveler? {
        return travelerRepository.allTravelersStore().get(id)?.let { mapper.readValue<Traveler>(it) }
    }

    @RequestMapping("/travelers/stats")
    fun travelersStats(): Map<String, Long> {
        return travelerRepository.allTravelersStateStore().all().use { it.asSequence().map { kv -> kv.key to kv.value }.toMap() }
    }
}
