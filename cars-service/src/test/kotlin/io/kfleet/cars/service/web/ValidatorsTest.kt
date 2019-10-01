package io.kfleet.cars.service.web

import io.kfleet.cars.service.repos.CreateOwnerParams
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class ValidatorsTest {

    @Test
    fun validateCreateOwnerParams() {
        val params = CreateOwnerParams(ownerId = "1", ownerName = "testName")

        StepVerifier.create(validate(params))
                .expectNextMatches {
                    it.ownerName == "testName" && it.ownerId == "1"
                }
                .expectComplete()
                .verify()
    }

    @Test
    fun validateErrorCreateOwnerParams() {
        val params = CreateOwnerParams(ownerId = "1", ownerName = "")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "ownerName invalid"
                }
                .verify()
    }
}
