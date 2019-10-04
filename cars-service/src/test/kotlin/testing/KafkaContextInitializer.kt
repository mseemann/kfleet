package testing

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

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
                    .waitingFor("registry", Wait.forHttp("/"))
        }

        class KDockerComposeContainer(file: File) : DockerComposeContainer<KDockerComposeContainer>(file)

        private fun defineDockerCompose() = KDockerComposeContainer(File("./../docker-compose.yml"))

    }
}

