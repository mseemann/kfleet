package io.kfleet.cars.service.repos

import io.kfleet.cars.service.processors.OwnerCommandsProcessorBinding
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.state.ReadOnlyWindowStore
import org.apache.kafka.streams.state.WindowStoreIterator
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
    lateinit var store: ReadOnlyWindowStore<String, CommandResponse>;


    @BeforeAll
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun findCommandResponseLocalById() {

        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyWindowStore<String, CommandResponse>>(
                ArgumentMatchers.eq(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val commandId = "1"
        val command = CommandResponse.newBuilder().apply {
            setCommandId(commandId)
            ressourceId = "1"
            status = CommandStatus.SUCCEEDED
        }.build()

        val windowStoreIterator = createWindowStoreIterator(listOf(KeyValue(1L, command)))

        BDDMockito.given(store.fetch(
                ArgumentMatchers.eq(commandId),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyLong())).willReturn(windowStoreIterator)

        StepVerifier.create(commandResponseRepo.findByIdLocal(commandId))
                .expectNext(command)
                .verifyComplete()
    }

    @Test
    fun findNoCommandResponseLocalById() {

        BDDMockito.given(interactiveQService.getQueryableStore<ReadOnlyWindowStore<String, CommandResponse>>(
                ArgumentMatchers.eq(OwnerCommandsProcessorBinding.OWNER_COMMANDS_RESPONSE_STORE),
                ArgumentMatchers.any())
        ).willReturn(store)

        val commandId = "1"

        val windowStoreIterator = createWindowStoreIterator(listOf<KeyValue<Long, CommandResponse>>())

        BDDMockito.given(store.fetch(
                ArgumentMatchers.eq(commandId),
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.anyLong())).willReturn(windowStoreIterator)

        StepVerifier.create(commandResponseRepo.findByIdLocal(commandId))
                .expectErrorMessage("command response with id: $commandId not found")
                .verify()
    }

    private fun createWindowStoreIterator(commands: List<KeyValue<Long, CommandResponse>>): WindowStoreIterator<CommandResponse> {
        val commandsIterator = commands.iterator()
        return object : WindowStoreIterator<CommandResponse> {

            override fun next(): KeyValue<Long, CommandResponse> {
                return commandsIterator.next()
            }

            override fun remove() {
            }

            override fun peekNextKey(): Long {
                return 1
            }

            override fun hasNext(): Boolean {
                return commandsIterator.hasNext()
            }

            override fun close() {
            }

        }

    }
}
