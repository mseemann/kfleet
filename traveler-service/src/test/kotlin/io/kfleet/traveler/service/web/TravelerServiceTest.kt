package io.kfleet.traveler.service.web


import io.kfleet.commands.CommandResponse
import io.kfleet.commands.CommandStatus
import io.kfleet.traveler.service.domain.*
import io.kfleet.traveler.service.repos.CommandsResponseRepository
import io.kfleet.traveler.service.repos.TravelerRepository
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import reactor.core.publisher.Mono
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.expect
import io.kfleet.traveler.service.domain.geoPositionCarRequest

@WebFluxTest(TravelerService::class)
@Import(TravelerRoutes::class)
@AutoConfigureWebTestClient(timeout = "15001") // backof retry is between 1 and 3 seconds; 5 times
class TravelerServiceTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockBean
    private lateinit var repo: TravelerRepository

    @MockBean
    private lateinit var commandResponseRepo: CommandsResponseRepository

    @Test
    fun travelerByIdTest() {
        val traveler = traveler {
            id = "1"
            name = "testname"
            email = "a@a.com"
        }

        BDDMockito.given(repo.findById("1")).willReturn(Mono.just(traveler))

        val response = webClient.get().uri("/traveler/1")
                .exchange()
                .expectStatus().isOk
                .expectBody(Traveler::class.java)
                .returnResult()

        expect(traveler) { response.responseBody }
    }

    @Test
    fun travelerByIdTest404() {

        BDDMockito.given(repo.findById("1")).willReturn(Mono.error(RuntimeException()))

        webClient.get().uri("/traveler/1")
                .exchange()
                .expectStatus().isNotFound
    }

    @Test
    fun createTraveler() {

        val params = NewTraveler(
                travelerId = "1",
                name = "testName",
                email = "a@a.com"
        )
        val traveler = traveler {
            id = "1"
            name = "testName"
            email = "a@a.com"
        }
        val createTravelerCommand = createTravelerCommand {
            commandId = "c1"
            travelerId = "1"
            name = "testName"
            email = "a@a.com"
        }

        BDDMockito
                .given(repo.submitCreateTravelerCommand(params))
                .willReturn(Mono.just(createTravelerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(createTravelerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(createTravelerCommand.getCommandId(), createTravelerCommand.getTravelerId(), CommandStatus.SUCCEEDED, null)))

        BDDMockito.given(repo.findById(traveler.getId())).willReturn(Mono.just(traveler))


        val response = webClient.post().uri("/traveler").body(BodyInserters.fromObject(params))
                .exchange()
                .expectStatus().isCreated
                .expectBody(Traveler::class.java)
                .returnResult()

        expect("1") { response.responseBody!!.getId() }
        expect(traveler) { response.responseBody }
    }

    @Test
    fun createTravelerBadRequest() {
        val params = NewTraveler(
                travelerId = "1",
                name = "",
                email = "a@a.com"
        )
        val result = webClient.post().uri("/traveler").body(BodyInserters.fromObject(params))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()
        assertEquals("travelerName invalid", result.responseBody)
    }

    @Test
    fun createTravelerBadRequestTravelerExists() {
        val params = NewTraveler(
                travelerId = "1",
                name = "testName",
                email = "a@a.com")

        val createTravelerCommand = createTravelerCommand {
            commandId = "c1"
            travelerId = "1"
            name = "testName"
            email = "a@a.com"
        }

        BDDMockito
                .given(repo.submitCreateTravelerCommand(params))
                .willReturn(Mono.just(createTravelerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(createTravelerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(createTravelerCommand.getCommandId(), createTravelerCommand.getTravelerId(), CommandStatus.REJECTED, "exists")))


        val response = webClient.post().uri("/traveler").body(BodyInserters.fromObject(params))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()

        expect("exists") { response.responseBody }
    }

    @Test
    fun deleteTraveler() {

        val params = DeleteTravelerParams(travelerId = "1")
        val deleteTravelerCommand = deleteTravelerCommand {
            commandId = "c1"
            travelerId = "1"
        }

        BDDMockito
                .given(repo.submitDeleteTravelerCommand(params))
                .willReturn(Mono.just(deleteTravelerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(deleteTravelerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(deleteTravelerCommand.getCommandId(), deleteTravelerCommand.getTravelerId(), CommandStatus.SUCCEEDED, null)))

        webClient.delete().uri("/traveler/1")
                .exchange()
                .expectStatus().isNoContent

    }

    @Test
    fun deleteTravelerBadRequestTravelerDidNotExists() {
        val params = DeleteTravelerParams(travelerId = "1")
        val deleteTravelerCommand = deleteTravelerCommand {
            commandId = "c1"
            travelerId = "1"
        }

        BDDMockito
                .given(repo.submitDeleteTravelerCommand(params))
                .willReturn(Mono.just(deleteTravelerCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(deleteTravelerCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(deleteTravelerCommand.getCommandId(), deleteTravelerCommand.getTravelerId(), CommandStatus.REJECTED, "did not exist")))


        val response = webClient.delete().uri("/traveler/1")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()

        expect("did not exist") { response.responseBody }
    }

    @Test
    fun deleteTravelerBadRequest() {
        val result = webClient.delete().uri("/traveler/ ")
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()
        assertEquals("travelerId invalid", result.responseBody)
    }

    @Test
    fun requestACar() {

        val requestACar = CarRequest(
                travelerId = "1",
                from = CarRequestGeoPosition(lat = 1.0, lng = 1.0),
                to = CarRequestGeoPosition(lat = 1.0, lng = 1.0),
                requestTime = Date(),
                id = "111")

        val requestCarCommand = carRequestCommand {
            commandId = "c1"
            travelerId = "1"
            from = geoPositionCarRequest {
                lat = requestACar.from.lat
                lng = requestACar.from.lng
            }
            to = geoPositionCarRequest {
                lat = requestACar.to.lat
                lng = requestACar.to.lng
            }
            requestTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmX")
                    .withZone(ZoneOffset.UTC)
                    .format(requestACar.requestTime.toInstant())
        }

        BDDMockito
                .given(repo.submitCarRequestTravelerCommand(requestACar))
                .willReturn(Mono.just(requestCarCommand))

        BDDMockito
                .given(commandResponseRepo.findCommandResponse(requestCarCommand.getCommandId()))
                .willReturn(Mono.just(CommandResponse(requestCarCommand.getCommandId(), requestCarCommand.getTravelerId(), CommandStatus.SUCCEEDED, null)))

        webClient.post().uri("/traveler/requestACar").body(BodyInserters.fromObject(requestACar))
                .exchange()
                .expectStatus().isNoContent

    }


    @Test
    fun requestACarBadRequest() {
        val requestACar = CarRequest(
                travelerId = "",
                from = CarRequestGeoPosition(lat = 1.0, lng = 1.0),
                to = CarRequestGeoPosition(lat = 1.0, lng = 1.0),
                requestTime = Date(),
                id = "111")

        val result = webClient.post().uri("/traveler/requestACar")
                .body(BodyInserters.fromObject(requestACar))
                .exchange()
                .expectStatus().isBadRequest
                .expectBody(String::class.java)
                .returnResult()
        assertEquals("travelerId invalid", result.responseBody)
    }
}
