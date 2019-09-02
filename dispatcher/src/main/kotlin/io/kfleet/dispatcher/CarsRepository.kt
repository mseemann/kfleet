package io.kfleet.dispatcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.dispatcher.configuration.DispatcherBinding
import io.kfleet.domain.Car
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@EnableBinding(DispatcherBinding::class)
class CarsRepository {

    @Autowired
    lateinit var interactiveQueryService: InteractiveQueryService

    val mapper = jacksonObjectMapper()

    @StreamListener
    fun test(@Input("cars") carTable: KTable<String, String>) {
        println(carTable.queryableStoreName())

//        carTable.toStream().foreach { s: String, car: String ->
//            println(s)
//            println(car)
//        }

        carTable.groupBy { key: String, rawCar: String ->
            val car: Car = mapper.readValue(rawCar)
            KeyValue(car.state.toString(), "1")
        }.count(Materialized.`as`("cars_by_state"))
                .toStream()
                .foreach { a: String, c: Long ->
                    println("$a -> $c")
                }
    }

    @RequestMapping("/cars")
    fun cars(): List<Car> {

//        val hostInfo = interactiveQueryService.getHostInfo("all-cars", "100", StringSerializer())
//
//
//        if (hostInfo == interactiveQueryService.currentHostInfo) {
//            println("is on this hoste")
//        }


        val carStore = interactiveQueryService
                .getQueryableStore("all-cars", QueryableStoreTypes.keyValueStore<String, String>())


        return carStore.all().use { it.asSequence().map { kv -> mapper.readValue<Car>(kv.value) }.toList() }
        
//        carStore.range("1", "5").use {
//            it.forEach { rawCarKV ->
//                val car: Car = mapper.readValue(rawCarKV.value)
//                println("${rawCarKV.key} ${car}")
//            }
//        }

    }

    @RequestMapping("/car/{id}")
    fun car(@PathVariable("id") id: String): Car? {
        val carStore = interactiveQueryService
                .getQueryableStore("all-cars", QueryableStoreTypes.keyValueStore<String, String>())

        return carStore.get(id)?.let { mapper.readValue<Car>(it) }
    }

    @RequestMapping("/cars-stats")
    fun carsStats(): Map<String, Long> {

        val carStatsStore = interactiveQueryService
                .getQueryableStore("cars_by_state", QueryableStoreTypes.keyValueStore<String, Long>())

        return carStatsStore.all().use { it.asSequence().map { it.key to it.value }.toMap() }

    }
}
