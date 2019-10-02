package io.kfleet.cars.service.processors

import io.kfleet.cars.service.repos.CreateOwnerParams
import io.kfleet.cars.service.repos.OwnerRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import reactor.test.StepVerifier
import java.io.File

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("it")
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class KafkaTest {


    @Autowired
    lateinit var repo: OwnerRepository

    @Test
    fun anyTest() {
        
        StepVerifier.create(repo.submitCreateOwnerCommand(CreateOwnerParams(ownerId = "1", ownerName = "test")))
                .expectNextMatches { command ->
                    command.getOwnerId() === "1"
                }
                .verifyComplete()
    }


}


class KafkaContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(context: ConfigurableApplicationContext) {
        instance.start()
        TestPropertyValues.of(
                "broker-url=${instance}"
        ).applyTo(context.environment)
    }


    companion object {
        private val instance: KDockerComposeContainer by lazy {
            defineDockerCompose()
                    .waitingFor("registry", Wait.forHttp("/"))
        }

        class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)

        private fun defineDockerCompose() = KDockerComposeContainer(File("./../docker-compose.yml"))

    }
}
