package testing

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import java.time.Duration

class KafkaContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    override fun initialize(context: ConfigurableApplicationContext) {
        instance.start()
        TestPropertyValues.of(
            "broker-url=${instance.getServiceHost("broker", 9092)}"
        ).applyTo(context.environment)
    }


    companion object {
        private val instance: KDockerComposeContainer by lazy {
            defineDockerCompose()
                .waitingFor(
                    "registry",
                    Wait.forHttp("/")
                        .withStartupTimeout(Duration.ofMinutes(15))
                )
        }

        class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)

        private fun defineDockerCompose() = KDockerComposeContainer(File("./../docker-compose-ci.yml"))

    }
}

