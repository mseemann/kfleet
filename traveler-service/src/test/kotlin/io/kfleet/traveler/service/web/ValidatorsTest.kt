package io.kfleet.traveler.service.web


import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.util.*

class ValidatorsTest {

    @Test
    fun validateCreateTravelerParams() {
        val params = NewTraveler(travelerId = "1", name = "testName", email = "a@a.com")

        StepVerifier.create(validateNewTraveler(params))
                .expectNextMatches {
                    it.name == "testName" && it.travelerId == "1" && it.email == "a@a.com"
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsName() {
        val params = NewTraveler(travelerId = "1", name = "", email = "a@a.com")

        StepVerifier.create(validateNewTraveler(params))
                .expectErrorMatches() {
                    it.message == "travelerName invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsId() {
        val params = NewTraveler(travelerId = "", name = "test", email = "a@a.com")

        StepVerifier.create(validateNewTraveler(params))
                .expectErrorMatches() {
                    it.message == "travelerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsEmail() {
        val params = NewTraveler(travelerId = "1", name = "test", email = "")

        StepVerifier.create(validateNewTraveler(params))
                .expectErrorMatches() {
                    it.message == "travelerEmail invalid"
                }
                .verify()
    }


    @Test
    fun validateErrorDeleteTravelerParamsId() {
        val params = DeleteTravelerParams(travelerId = "")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "travelerId invalid"
                }
                .verify()
    }

    @Test
    fun validateCarRequestTravelerId() {
        val params = CarRequest(
                travelerId = "",
                from = CarRequestGeoPosition(1.0, 1.0),
                to = CarRequestGeoPosition(1.0, 1.0),
                requestTime = Date(),
                id = "111")

        StepVerifier.create(validateCarRequest(params))
                .expectErrorMatches() {
                    it.message == "travelerId invalid"
                }
                .verify()
    }

}
