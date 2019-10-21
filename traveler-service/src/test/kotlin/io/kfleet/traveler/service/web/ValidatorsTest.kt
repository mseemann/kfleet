package io.kfleet.traveler.service.web


import io.kfleet.traveler.service.repos.DeleteTravelerParams
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class ValidatorsTest {

    @Test
    fun validateCreateTravelerParams() {
        val params = NewTraveler(id = "1", name = "testName", email = "a@a.com")

        StepVerifier.create(validateNewTraveler(params))
                .expectNextMatches {
                    it.name == "testName" && it.id == "1" && it.email == "a@a.com"
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsName() {
        val params = NewTraveler(id = "1", name = "", email = "a@a.com")

        StepVerifier.create(validateNewTraveler(params))
                .expectErrorMatches() {
                    it.message == "travelerName invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsId() {
        val params = NewTraveler(id = "", name = "test", email = "a@a.com")

        StepVerifier.create(validateNewTraveler(params))
                .expectErrorMatches() {
                    it.message == "travelerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateTravelerParamsEmail() {
        val params = NewTraveler(id = "1", name = "test", email = "")

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

}
