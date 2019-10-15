package io.kfleet.owner.service.web


import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.owner.service.domain.*
import io.kfleet.owner.service.repos.*
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
@Import(OwnerRoutes::class)
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
        val owner = owner {
            id = "1"
            name = "testname"
        }

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

        val params = CreateOwnerParams(ownerId = "1", ownerName = "testName")
        val owner = owner {
            id = "1"
            name = "testName"
        }
        val createOwnerCommand = createOwnerCommand {
            commandId = "c1"
            ownerId = "1"
            name = "testName"
        }

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
        val params = CreateOwnerParams(ownerId = "1", ownerName = "testName")
        val createOwnerCommand = createOwnerCommand {
            commandId = "c1"
            ownerId = "1"
            name = "testName"
        }

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


    @Test
    fun updateOwner() {

        val params = UpdateOwnerParams(ownerId = "1", ownerName = "testName")
        val owner = owner {
            id = "1"
            name = "testName"
        }
        val updateOwnerCommand = updateOwnerNameCommand {
            commandId = "c1"
            ownerId = "1"
            name = "testName"
        }

        BDDMockito
                .given(repo.submitUpdateOwnerNameCommand(params))
                .willReturn(Mono.just(updateOwnerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(updateOwnerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(updateOwnerCommand.getCommandId(), updateOwnerCommand.getOwnerId(), CommandStatus.SUCCEEDED, null)))

        BDDMockito.given(repo.findById(owner.getId())).willReturn(Mono.just(owner))


        val response = webClient.put().uri("/owners/1/testName")
                .exchange()
                .expectStatus().isOk
                .expectBody(Owner::class.java)
                .returnResult()

        expect("1") { response.responseBody!!.getId() }
        expect(owner) { response.responseBody }
    }

    @Test
    fun updateOwnerBadRequest() {
        val result = webClient.put().uri("/owners/1/ ")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()
        assertEquals("ownerName invalid", result.responseBody)
    }

    @Test
    fun updateOwnerBadRequestOwnerDidNotExists() {
        val params = UpdateOwnerParams(ownerId = "1", ownerName = "testName")
        val updateOwnerCommand = updateOwnerNameCommand {
            commandId = "c1"
            ownerId = "1"
            name = "testName"
        }


        BDDMockito
                .given(repo.submitUpdateOwnerNameCommand(params))
                .willReturn(Mono.just(updateOwnerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(updateOwnerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(updateOwnerCommand.getCommandId(), updateOwnerCommand.getOwnerId(), CommandStatus.REJECTED, "did not exist")))


        val response = webClient.put().uri("/owners/1/testName")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()

        expect("did not exist") { response.responseBody }
    }

    @Test
    fun deleteOwner() {

        val params = DeleteOwnerParams(ownerId = "1")
        val deleteOwnerCommand = deleteOwnerCommand {
            commandId = "c1"
            ownerId = "1"
        }

        BDDMockito
                .given(repo.submitDeleteOwnerCommand(params))
                .willReturn(Mono.just(deleteOwnerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(deleteOwnerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(deleteOwnerCommand.getCommandId(), deleteOwnerCommand.getOwnerId(), CommandStatus.SUCCEEDED, null)))

        webClient.delete().uri("/owners/1")
                .exchange()
                .expectStatus().isNoContent

    }

    @Test
    fun deleteOwnerBadRequestOwnerDidNotExists() {
        val params = DeleteOwnerParams(ownerId = "1")
        val deleteOwnerCommand = deleteOwnerCommand {
            commandId = "c1"
            ownerId = "1"
        }

        BDDMockito
                .given(repo.submitDeleteOwnerCommand(params))
                .willReturn(Mono.just(deleteOwnerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(deleteOwnerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(deleteOwnerCommand.getCommandId(), deleteOwnerCommand.getOwnerId(), CommandStatus.REJECTED, "did not exist")))


        val response = webClient.delete().uri("/owners/1")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()

        expect("did not exist") { response.responseBody }
    }

    @Test
    fun deleteOwnerBadRequest() {
        val result = webClient.delete().uri("/owners/ ")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()
        assertEquals("ownerId invalid", result.responseBody)
    }
}
