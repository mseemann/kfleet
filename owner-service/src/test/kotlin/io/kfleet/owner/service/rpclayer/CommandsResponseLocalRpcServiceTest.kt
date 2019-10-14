package io.kfleet.owner.service.rpclayer

import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.owner.service.configuration.JacksonObjectMapper
import io.kfleet.owner.service.repos.CommandsResponseLocalRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import kotlin.test.expect

@WebFluxTest(CommandsResponseLocalRpcService::class)
@Import(JacksonObjectMapper::class, CommandsResponseRpcRoutes::class)
class CommandsResponseLocalRpcServiceTest {


    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: CommandsResponseLocalRepository

    @Test
    fun commandResponseByIdTest() {

        val commandResponse = CommandResponse("1", "1", CommandStatus.SUCCEEDED, null)

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.just(commandResponse))

        val response = webClient.get().uri("/rpc/command-response/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(CommandResponse::class.java)
                .returnResult()

        expect(commandResponse) { response.responseBody }
    }

    @Test
    fun commandResponseByIdTest404() {

        BDDMockito.given(repo.findByIdLocal("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/rpc/command-response/1")
                .exchange()
                .expectStatus().isNotFound
    }
}
