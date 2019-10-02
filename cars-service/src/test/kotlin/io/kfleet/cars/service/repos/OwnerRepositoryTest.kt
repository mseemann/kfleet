package io.kfleet.cars.service.repos


import io.kfleet.cars.service.WebClientUtil
import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.OwnerFactory
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.state.HostInfo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.*
import org.mockito.BDDMockito.*
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.expect


class OwnerRepositoryTest {

    @Captor
    lateinit var captureMsg: ArgumentCaptor<Message<CreateOwnerCommand>>

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

    @Test
    fun sendCreateOwnerCommand() {
        given(outputChannel.send(captureMsg.capture())).willReturn(true)

        val result = ownerRepo.submitCreateOwnerCommand(createOwnerParams)

        val returnedCommand = result.block()!!
        expect(true) { returnedCommand is CreateOwnerCommand }
        expect(createOwnerParams.ownerId) { returnedCommand.getOwnerId() }
        expect(createOwnerParams.ownerName) { returnedCommand.getName() }

        val sendMessage = captureMsg.value
        val command = sendMessage.payload
        val messageKey = sendMessage.headers.get(KafkaHeaders.MESSAGE_KEY)
        expect(createOwnerParams.ownerId) { messageKey }
        expect(createOwnerParams.ownerId) { command.getOwnerId() }
        expect(createOwnerParams.ownerName) { command.getName() }
    }

    @Test
    fun sendCreateOwnerCommandFailure() {

        given(outputChannel.send(captureMsg.capture())).willReturn(false)

        StepVerifier.create(ownerRepo.submitCreateOwnerCommand(createOwnerParams))
                .expectErrorMessage("CreateOwnerCommand coud not be send.")
                .verify()
    }

    @Test
    fun sendCreateOwnerCommandUnknownFailure() {

        given(outputChannel.send(captureMsg.capture())).willThrow(RuntimeException())

        StepVerifier.create(ownerRepo.submitCreateOwnerCommand(createOwnerParams))
                .expectError()
                .verify()
    }

    @Test
    fun findOwnerById() {

        val owner = OwnerFactory.create("1", "test")
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
