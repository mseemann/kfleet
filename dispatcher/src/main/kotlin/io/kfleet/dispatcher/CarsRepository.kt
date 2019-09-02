package io.kfleet.dispatcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kfleet.dispatcher.configuration.DispatcherBinding
import io.kfleet.domain.Car
import org.apache.coyote.http11.Constants.a
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.ForeachAction
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Serialized
import org.apache.kafka.streams.processor.StateStore


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
            KeyValue(car.state.toString(), rawCar)
        }.count(Materialized.`as`("cars_by_state"))
                .toStream()
                .foreach { a: String, c: Long ->
                    println("$a -> $c")
                }
    }

    @RequestMapping("/cars")
    fun cars(): List<Car> {

        val hostInfo = interactiveQueryService.getHostInfo("all-cars", "100", StringSerializer())


        if (hostInfo == interactiveQueryService.currentHostInfo) {
            println("is on this hoste")
        }


        val carStore = interactiveQueryService
                .getQueryableStore("all-cars", QueryableStoreTypes.keyValueStore<String, String>())

        val result = mutableListOf<Car>()
        carStore.all().use {
            it.forEach { rawCarKV ->
                val car: Car = mapper.readValue(rawCarKV.value)
                // println("${it.key} ${car}")
                result.add(car)
            }
        }

        val rawCar = carStore.get("100")
        if (rawCar != null) {
            val car: Car = mapper.readValue(rawCar)
            println(car)
        }

        carStore.range("1", "5").use {
            it.forEach { rawCarKV ->
                val car: Car = mapper.readValue(rawCarKV.value)
                println("${rawCarKV.key} ${car}")
            }
        }

        return result
    }
}
