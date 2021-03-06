package io.kfleet.traveler.service.repos


import io.kfleet.common.WebClientUtil
import io.kfleet.traveler.service.commands.CarRequestCommand
import io.kfleet.traveler.service.commands.CreateTravelerCommand
import io.kfleet.traveler.service.commands.DeleteTravelerCommand
import io.kfleet.traveler.service.domain.Traveler
import io.kfleet.traveler.service.domain.traveler
import io.kfleet.traveler.service.web.CarRequest
import io.kfleet.traveler.service.web.CarRequestGeoPosition
import io.kfleet.traveler.service.web.DeleteTravelerParams
import io.kfleet.traveler.service.web.NewTraveler
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.state.HostInfo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*
import kotlin.test.expect


class TravelerRepositoryTest {

    @Mock
    lateinit var outputChannel: MessageChannel

    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var webclientUtils: WebClientUtil

    @InjectMocks
    lateinit var travelerRepo: TravelerRepository

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    private val createTravelerParams = NewTraveler(
            travelerId = "1",
            name = "testName",
            email = "a@a.com")
    private val deleteTravelerParams = DeleteTravelerParams(travelerId = "1")

    private val carRequest = CarRequest(
            travelerId = "",
            from = CarRequestGeoPosition(lat = 1.0, lng = 1.0),
            to = CarRequestGeoPosition(lat = 1.0, lng = 1.0),
            requestTime = Date(),
            id = "111")

    @Test
    fun sendCreateTravelerCommand() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(true)

        val result = travelerRepo.submitCreateTravelerCommand(createTravelerParams)

        val returnedCommand = result.block()!!
        expect(createTravelerParams.travelerId) { returnedCommand.getTravelerId() }
        expect(createTravelerParams.name) { returnedCommand.getName() }
        expect(createTravelerParams.email) { returnedCommand.getEmail() }

        val sendMessage = capture.value
        val command = sendMessage.payload as CreateTravelerCommand
        val messageKey = sendMessage.headers[KafkaHeaders.MESSAGE_KEY]
        expect(createTravelerParams.travelerId) { messageKey }
        expect(createTravelerParams.travelerId) { command.getTravelerId() }
        expect(createTravelerParams.name) { command.getName() }
    }

    @Test
    fun sendCreateTravelerCommandFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(false)

        StepVerifier.create(travelerRepo.submitCreateTravelerCommand(createTravelerParams))
                .expectErrorMessage("CreateTravelerCommand coud not be send.")
                .verify()
    }

    @Test
    fun sendCreateTravelerCommandUnknownFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willThrow(RuntimeException())
                // reset throw for the next call
                .willReturn(true)

        StepVerifier.create(travelerRepo.submitCreateTravelerCommand(createTravelerParams))
                .expectError()
                .verify()
    }


    @Test
    fun sendTravelerDeleteCommand() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(true)

        val result = travelerRepo.submitDeleteTravelerCommand(deleteTravelerParams)

        val returnedCommand = result.block()!!
        expect(deleteTravelerParams.travelerId) { returnedCommand.getTravelerId() }

        val sendMessage = capture.value
        val command = sendMessage.payload as DeleteTravelerCommand
        val messageKey = sendMessage.headers[KafkaHeaders.MESSAGE_KEY]
        expect(deleteTravelerParams.travelerId) { messageKey }
        expect(deleteTravelerParams.travelerId) { command.getTravelerId() }
    }

    @Test
    fun sendTravelerDeleteCommandFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(false)

        StepVerifier.create(travelerRepo.submitDeleteTravelerCommand(deleteTravelerParams))
                .expectErrorMessage("DeleteTravelerCommand could not be send.")
                .verify()
    }

    @Test
    fun sendDeleteTravelerCommandUnknownFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willThrow(RuntimeException())
                // reset throw for the next call
                .willReturn(true)

        StepVerifier.create(travelerRepo.submitDeleteTravelerCommand(deleteTravelerParams))
                .expectError()
                .verify()
    }

    @Test
    fun findTravelerById() {

        val traveler = traveler {
            id = "1"
            name = "test"
            email = "a@a.com"
        }
        val hostInfo = HostInfo("localhost", 8284)

        given(interactiveQService.getHostInfo(anyString(), anyString(), any<StringSerializer>()))
                .willReturn(hostInfo)

        given(webclientUtils.doGet(hostInfo, "/rpc/traveler/1", Traveler::class.java))
                .willReturn(Mono.just(traveler))

        StepVerifier.create(travelerRepo.findById(traveler.getId()))
                .expectNext(traveler)
                .verifyComplete()

    }

    @Test
    fun sendCarRequestCommand() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(true)

        val result = travelerRepo.submitCarRequestTravelerCommand(carRequest)

        val returnedCommand = result.block()!!
        expect(carRequest.travelerId) { returnedCommand.getTravelerId() }

        val sendMessage = capture.value
        val command = sendMessage.payload as CarRequestCommand
        val messageKey = sendMessage.headers[KafkaHeaders.MESSAGE_KEY]
        expect(carRequest.travelerId) { messageKey }
        expect(carRequest.travelerId) { command.getTravelerId() }
    }

    @Test
    fun sendCarREquestCommandFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(false)

        StepVerifier.create(travelerRepo.submitCarRequestTravelerCommand(carRequest))
                .expectErrorMessage("CarRequestCommand could not be send.")
                .verify()
    }

    @Test
    fun sendCaRRequestCommandUnknownFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willThrow(RuntimeException())
                // reset throw for the next call
                .willReturn(true)

        StepVerifier.create(travelerRepo.submitCarRequestTravelerCommand(carRequest))
                .expectError()
                .verify()
    }

}
