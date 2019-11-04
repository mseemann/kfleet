package io.kfleet.riderequestdispatcher.processor


import io.kfleet.common.headers
import io.kfleet.domain.events.*
import io.kfleet.riderequestdispatcher.simulation.CarsLocationEventsOutBindings
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import testing.KafkaContextInitializer

@EnabledIfEnvironmentVariable(named = "ENV", matches = "ci")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class DispatcherProcessorTest {

    @Autowired
    @Output(CarsLocationEventsOutBindings.CARS_LOCATION)
    lateinit var carLocationEventOutputChannel: MessageChannel


    @Test
    fun submitCarLocationEvent() {

        val carId = 10
        val pos = geoPositionCarLocation {
            lat = OsloLatRange.get(1)
            lng = OsloLngRange.get(0)
        }
        val carEvent = carLocationChangedEvent {
            setCarId("$carId")
            geoPosition = pos
            geoPositionIndex = pos.toQuadrantIndex()
        }
        val message = MessageBuilder.createMessage(carEvent, headers(carId))

        carLocationEventOutputChannel.send(message)

        // FIXME store lookup!
        Thread.sleep(5000)

    }
}

