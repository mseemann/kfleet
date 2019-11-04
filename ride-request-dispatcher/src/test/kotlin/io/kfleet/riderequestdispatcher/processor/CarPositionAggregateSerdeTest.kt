package io.kfleet.riderequestdispatcher.processor


import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.expect

class CarPositionAggregateSerdeTest {

    @Test
    fun serializeCarPositionAggregate() {

        val agg = CarPositionAggregate()
        agg.add("1", "c1")

        val serde = CarPositionAggregateSerde()

        val serialized = serde.serializer().serialize("test", agg)

        val deserialized = serde.deserializer().deserialize("test", serialized)

        expect(agg.quadrants) { deserialized.quadrants }
    }

    @Test
    fun deserializeCarPositionAggregate() {

        val agg = CarPositionAggregate()
        agg.add("1", "c1")

        val serde = CarPositionAggregateSerde()

        val deserializedNull = serde.deserializer().deserialize("test", null)
        assertNull(deserializedNull)

        val deserializedEmpty = serde.deserializer().deserialize("test", byteArrayOf())
        assertNull(deserializedEmpty)
    }
}
