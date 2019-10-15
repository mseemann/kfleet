package io.kfleet.owner.service.web


import io.kfleet.owner.service.domain.CarModel
import io.kfleet.owner.service.repos.*
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
    fun validateErrorCreateOwnerParamsName() {
        val params = CreateOwnerParams(ownerId = "1", ownerName = "")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "ownerName invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorCreateOwnerParamsId() {
        val params = CreateOwnerParams(ownerId = "", ownerName = "test")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "ownerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorUpdateOwnerParamsId() {
        val params = UpdateOwnerParams(ownerId = "", ownerName = "test")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "ownerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorDeleteOwnerParamsId() {
        val params = DeleteOwnerParams(ownerId = "")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "ownerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorRegisterCarParams() {
        val params = RegisterCarParams(ownerId = "", newCar = NewCar(CarModel.ModelX))

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "ownerId invalid"
                }
                .verify()
    }

    @Test
    fun validateErrorDeregisterCarParams() {
        val params = DeregisterCarParams(ownerId = "x", carId = "")

        StepVerifier.create(validate(params))
                .expectErrorMatches() {
                    it.message == "carId invalid"
                }
                .verify()
    }
}
