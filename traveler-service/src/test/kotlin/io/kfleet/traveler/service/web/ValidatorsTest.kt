package io.kfleet.traveler.service.web


import io.kfleet.traveler.service.repos.CreateTravelerParams
import io.kfleet.traveler.service.repos.DeleteTravelerParams
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class ValidatorsTest {

    @Test
    fun validateCreateTravelerParams() {
        val params = CreateTravelerParams(travelerId = "1", travelerName = "testName", travelerEmail = "a@a.com")

        StepVerifier.create(validate(params))
                .expectNextMatches {
                    it.travelerName == "testName" && it.travelerId == "1" && it.travelerEmail == "a@a.com"
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsName() {
        val params = CreateTravelerParams(travelerId = "1", travelerName = "", travelerEmail = "a@a.com")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "travelerName invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsId() {
        val params = CreateTravelerParams(travelerId = "", travelerName = "test", travelerEmail = "a@a.com")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "travelerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsEmail() {
        val params = CreateTravelerParams(travelerId = "1", travelerName = "test", travelerEmail = "")

        StepVerifier.create(validate(params))
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

}
