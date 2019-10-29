package io.kfleet.car.service.processor

import io.kfleet.domain.events.car.CarLocationChangedEvent
import io.kfleet.domain.events.toQuadrantIndex
import mu.KotlinLogging
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Serialized
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.StreamListener
import java.io.*

private val log = KotlinLogging.logger {}

interface CarLocationsProcessorBinding {

    companion object {
        const val CARS_LOCATIONS = "car_locations_in"
        const val CAR_LOCATION_STORE = "car_locations_store"
    }

    @Input(CARS_LOCATIONS)
    fun inputCarLocations(): KTable<String, CarLocationChangedEvent>

}

@EnableBinding(CarLocationsProcessorBinding::class)
class CarLocationsProcessor() {


    @StreamListener
    fun carLocationUpdates(@Input(CarLocationsProcessorBinding.CARS_LOCATIONS) carLocations: KTable<String, CarLocationChangedEvent>) {

        carLocations
                .groupBy({ _, carLocationChangedEvent ->
                    KeyValue(carLocationChangedEvent.getGeoPosition().toQuadrantIndex(),
                            carLocationChangedEvent.getCarId())
                }, Serialized.with(Serdes.String(), Serdes.String()))
                .aggregate(
                        { CarPositionAggregate() },
                        { quadrant, carId, a -> a.add(quadrant, carId) },
                        { quadrant, carId, a -> a.sub(quadrant, carId) },
                        Materialized.`as`<String, CarPositionAggregate, KeyValueStore<Bytes, ByteArray>>
                        (CarLocationsProcessorBinding.CAR_LOCATION_STORE)
                                .withKeySerde(Serdes.String())
                                .withValueSerde(CarPositionAggregateSerde())
                )
                .toStream()
                .peek { quadrant, agg -> log.debug { "$quadrant -> $agg" } }
    }
}

class CarPositionAggregateSerde : Serde<CarPositionAggregate> {

    override fun deserializer(): Deserializer<CarPositionAggregate> {
        return object : Deserializer<CarPositionAggregate> {
            override fun configure(map: Map<String, *>, b: Boolean) {
            }

            override fun deserialize(s: String, bytes: ByteArray?): CarPositionAggregate? {
                if (bytes == null || bytes.size == 0) {
                    return null
                }

                val dataInputStream = ObjectInputStream(ByteArrayInputStream(bytes))
                val result = dataInputStream.readObject() as CarPositionAggregate
                return result
            }

            override fun close() {
            }
        }
    }

    override fun close() {

    }

    override fun serializer(): Serializer<CarPositionAggregate> {
        return object : Serializer<CarPositionAggregate> {
            override fun configure(map: Map<String, *>, b: Boolean) {}

            override fun serialize(s: String, carPositionAggregate: CarPositionAggregate): ByteArray {
                val out = ByteArrayOutputStream()
                val dataOutputStream = ObjectOutputStream(out)
                dataOutputStream.writeObject(carPositionAggregate)
                dataOutputStream.flush()
                return out.toByteArray()
            }

            override fun close() {
            }
        }
    }

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
    }

}


class CarPositionAggregate : Serializable {

    val serialVersionUID = 1L

    val quadrants = mutableMapOf<String, Set<String>>()

    fun add(quadrant: String, carId: String): CarPositionAggregate {
        log.debug { "add $carId to $quadrant" }
        val carEvents = quadrants.getOrDefault(quadrant, setOf()).toMutableSet()
        carEvents.add(carId)
        quadrants.put(quadrant, carEvents)
        return this
    }

    fun sub(quadrant: String, carId: String): CarPositionAggregate {
        log.debug { "sub $carId from $quadrant" }
        val carEvents = quadrants.getOrDefault(quadrant, setOf())
        quadrants.put(quadrant, carEvents.minus(carId))
        return this
    }

    override fun toString(): String {
        return "CarPositionAggregate(quadrants=$quadrants)"
    }

}
