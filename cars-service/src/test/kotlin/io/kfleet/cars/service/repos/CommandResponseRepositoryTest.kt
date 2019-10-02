package io.kfleet.cars.service.repos


import io.kfleet.cars.service.WebClientUtil
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.state.HostInfo
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.*
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import reactor.core.publisher.Mono
import reactor.test.StepVerifier


class CommandResponseRepositoryTest {


    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @Mock
    lateinit var webclientUtils: WebClientUtil

    @InjectMocks
    lateinit var commandResponseRepo: CommandsResponseRepository

    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }


    @Test
    fun findCommandResponseById() {

        val cResponse = CommandResponse.newBuilder().apply {
            commandId = "1"
            status = CommandStatus.SUCCEEDED
            ressourceId = "2"
        }.build()

        val hostInfo = HostInfo("localhost", 8084)

        given(interactiveQService.getHostInfo(anyString(), anyString(), any<StringSerializer>()))
                .willReturn(hostInfo)

        given(webclientUtils.doGet(hostInfo, "/rpc/command-response/1", CommandResponse::class.java))
                .willReturn(Mono.just(cResponse))

        StepVerifier.create(commandResponseRepo.findCommandResponse(cResponse.getCommandId()))
                .expectNext(cResponse)
                .verifyComplete()

    }
}
