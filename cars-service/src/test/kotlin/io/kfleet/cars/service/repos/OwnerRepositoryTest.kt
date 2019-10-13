package io.kfleet.cars.service.repos


import io.kfleet.cars.service.WebClientUtil
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.commands.DeleteOwnerCommand
import io.kfleet.cars.service.commands.UpdateOwnernameCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.owner
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
import kotlin.test.expect


class OwnerRepositoryTest {

    @Mock
    lateinit var outputChannel: MessageChannel

    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var webclientUtils: WebClientUtil

    @InjectMocks
    lateinit var ownerRepo: OwnerRepository

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    val createOwnerParams = CreateOwnerParams(ownerId = "1", ownerName = "testName")
    val updateOwnerParams = UpdateOwnerParams(ownerId = "1", ownerName = "testNameNew")
    val deleteOwnerParams = DeleteOwnerParams(ownerId = "1")

    @Test
    fun sendCreateOwnerCommand() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(true)

        val result = ownerRepo.submitCreateOwnerCommand(createOwnerParams)

        val returnedCommand = result.block()!!
        expect(createOwnerParams.ownerId) { returnedCommand.getOwnerId() }
        expect(createOwnerParams.ownerName) { returnedCommand.getName() }

        val sendMessage = capture.value
        val command = sendMessage.payload as CreateOwnerCommand
        val messageKey = sendMessage.headers.get(KafkaHeaders.MESSAGE_KEY)
        expect(createOwnerParams.ownerId) { messageKey }
        expect(createOwnerParams.ownerId) { command.getOwnerId() }
        expect(createOwnerParams.ownerName) { command.getName() }
    }

    @Test
    fun sendCreateOwnerCommandFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(false)

        StepVerifier.create(ownerRepo.submitCreateOwnerCommand(createOwnerParams))
                .expectErrorMessage("CreateOwnerCommand coud not be send.")
                .verify()
    }

    @Test
    fun sendCreateOwnerCommandUnknownFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willThrow(RuntimeException())
                // reset throw for the next call
                .willReturn(true)

        StepVerifier.create(ownerRepo.submitCreateOwnerCommand(createOwnerParams))
                .expectError()
                .verify()
    }

    @Test
    fun sendOwnerUpdateCommand() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(true)

        val result = ownerRepo.submitUpdateOwnerNameCommand(updateOwnerParams)

        val returnedCommand = result.block()!!
        expect(updateOwnerParams.ownerId) { returnedCommand.getOwnerId() }
        expect(updateOwnerParams.ownerName) { returnedCommand.getName() }

        val sendMessage = capture.value
        val command = sendMessage.payload as UpdateOwnernameCommand
        val messageKey = sendMessage.headers.get(KafkaHeaders.MESSAGE_KEY)
        expect(updateOwnerParams.ownerId) { messageKey }
        expect(updateOwnerParams.ownerId) { command.getOwnerId() }
        expect(updateOwnerParams.ownerName) { command.getName() }
    }

    @Test
    fun sendOwnerUpdateCommandFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(false)

        StepVerifier.create(ownerRepo.submitUpdateOwnerNameCommand(updateOwnerParams))
                .expectErrorMessage("UpdateOwnerNameCommand coud not be send.")
                .verify()
    }

    @Test
    fun sendUpdateOwnerCommandUnknownFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willThrow(RuntimeException())
                // reset throw for the next call
                .willReturn(true)

        StepVerifier.create(ownerRepo.submitUpdateOwnerNameCommand(updateOwnerParams))
                .expectError()
                .verify()
    }

    @Test
    fun sendOwnerDeleteCommand() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(true)

        val result = ownerRepo.submitDeleteOwnerCommand(deleteOwnerParams)

        val returnedCommand = result.block()!!
        expect(updateOwnerParams.ownerId) { returnedCommand.getOwnerId() }

        val sendMessage = capture.value
        val command = sendMessage.payload as DeleteOwnerCommand
        val messageKey = sendMessage.headers.get(KafkaHeaders.MESSAGE_KEY)
        expect(updateOwnerParams.ownerId) { messageKey }
        expect(updateOwnerParams.ownerId) { command.getOwnerId() }
    }

    @Test
    fun sendOwnerDeleteCommandFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willReturn(false)

        StepVerifier.create(ownerRepo.submitDeleteOwnerCommand(deleteOwnerParams))
                .expectErrorMessage("DeleteOwnerCommand coud not be send.")
                .verify()
    }

    @Test
    fun sendDeleteOwnerCommandUnknownFailure() {
        val capture = ArgumentCaptor.forClass(Message::class.java)
        given(outputChannel.send(capture.capture())).willThrow(RuntimeException())
                // reset throw for the next call
                .willReturn(true)

        StepVerifier.create(ownerRepo.submitDeleteOwnerCommand(deleteOwnerParams))
                .expectError()
                .verify()
    }

    @Test
    fun findOwnerById() {

        val owner = owner {
            id = "1"
            name = "test"
        }
        val hostInfo = HostInfo("localhost", 8084)

        given(interactiveQService.getHostInfo(anyString(), anyString(), any<StringSerializer>()))
                .willReturn(hostInfo)

        given(webclientUtils.doGet(hostInfo, "/rpc/owner/1", Owner::class.java))
                .willReturn(Mono.just(owner))

        StepVerifier.create(ownerRepo.findById(owner.getId()))
                .expectNext(owner)
                .verifyComplete()

    }
}
