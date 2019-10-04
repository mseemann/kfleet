package io.kfleet.cars.service.web

import io.kfleet.cars.service.commands.CreateOwnerCommand
import io.kfleet.cars.service.configuration.JacksonObjectMapper
import io.kfleet.cars.service.domain.Owner
import io.kfleet.cars.service.domain.OwnerFactory
import io.kfleet.cars.service.repos.CommandsResponseRepository
import io.kfleet.cars.service.repos.CreateOwnerParams
import io.kfleet.cars.service.repos.OwnerRepository
import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import kotlin.test.assertEquals
import kotlin.test.expect

@WebFluxTest(OwnerService::class)
@Import(JacksonObjectMapper::class, OwnerRoutes::class)
@AutoConfigureWebTestClient(timeout = "15001") // backof retry is between 1 and 3 seconds; 5 times
class OwnerServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: OwnerRepository

    @MockBean
    private lateinit var commandResponseRepo: CommandsResponseRepository

    @Test
    fun ownerByIdTest() {
        val owner = OwnerFactory.create(id = "1", name = "testname")

        BDDMockito.given(repo.findById("1")).willReturn(Mono.just(owner))

        val response = webClient.get().uri("/owners/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(Owner::class.java)
                .returnResult()

        expect(owner) { response.responseBody }
    }

    @Test
    fun ownerByIdTest404() {

        BDDMockito.given(repo.findById("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/owners/1")
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun createOwner() {

        val params = CreateOwnerParams("1", "testName")
        val owner = OwnerFactory.create("1", "testName")
        val createOwnerCommand = CreateOwnerCommand("c1", "1", "testName")

        BDDMockito
                .given(repo.submitCreateOwnerCommand(params))
                .willReturn(Mono.just(createOwnerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(createOwnerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(createOwnerCommand.getCommandId(), createOwnerCommand.getOwnerId(), CommandStatus.SUCCEEDED, null)))

        BDDMockito.given(repo.findById(owner.getId())).willReturn(Mono.just(owner))


        val response = webClient.post().uri("/owners/1/testName")
                .exchange()
                .expectStatus().isCreated
                .expectBody(Owner::class.java)
                .returnResult()

        expect("1") { response.responseBody!!.getId() }
        expect(owner) { response.responseBody }
    }

    @Test
    fun createOwnerBadRequest() {
        val result = webClient.post().uri("/owners/1/ ")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()
        assertEquals("ownerName invalid", result.responseBody)
    }

    @Test
    fun createOwnerBadRequestOwnerExists() {
        val params = CreateOwnerParams("1", "testName")
        val createOwnerCommand = CreateOwnerCommand("c1", "1", "testName")

        BDDMockito
                .given(repo.submitCreateOwnerCommand(params))
                .willReturn(Mono.just(createOwnerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(createOwnerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(createOwnerCommand.getCommandId(), createOwnerCommand.getOwnerId(), CommandStatus.REJECTED, "exists")))


        val response = webClient.post().uri("/owners/1/testName")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()

        expect("exists") { response.responseBody }
    }
}
