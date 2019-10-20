package io.kfleet.traveler.service.repos


import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.traveler.service.configuration.TRAVELER_COMMANDS_RESPONSE_STORE
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.*
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService
import reactor.test.StepVerifier

class CommandsResponseLocalRepositoryTest {
    @Mock
    lateinit var interactiveQService: InteractiveQueryService

    @InjectMocks
    lateinit var commandResponseRepo: CommandsResponseLocalRepository

    @Mock
    lateinit var store: ReadOnlyKeyValueStore<String, CommandResponse>;


    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun findCommandResponseLocalById() {

        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, CommandResponse>>(
                ArgumentMatchers.eq(TRAVELER_COMMANDS_RESPONSE_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val commandId = "1"
        val command = CommandResponse.newBuilder().apply {
            setCommandId(commandId)
            ressourceId = "1"
            status = CommandStatus.SUCCEEDED
        }.build()

        BDDMockito.given(store.get(commandId)).willReturn(command)

        StepVerifier.create(commandResponseRepo.findByIdLocal(commandId))
                .expectNext(command)
                .verifyComplete()
    }

    @Test
    fun findNoCommandResponseLocalById() {

        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyKeyValueStore<String, CommandResponse>>(
                ArgumentMatchers.eq(TRAVELER_COMMANDS_RESPONSE_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val commandId = "1"

        BDDMockito.given(store.get(commandId)).willReturn(null)
        
        StepVerifier.create(commandResponseRepo.findByIdLocal(commandId))
                .expectErrorMessage("command response with id: $commandId not found")
                .verify()
    }

}
