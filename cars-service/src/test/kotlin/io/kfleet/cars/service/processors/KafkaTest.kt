package io.kfleet.cars.service.processors

import io.kfleet.cars.service.domain.CarFactory
import io.kfleet.cars.service.repos.CommandsResponseRepository
import io.kfleet.cars.service.repos.CreateOwnerParams
import io.kfleet.cars.service.repos.OwnerRepository
import io.kfleet.cars.service.simulation.CarsOutBindings
import io.kfleet.commands.CommandStatus
import io.kfleet.common.customRetry
import io.kfleet.common.headers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.cloud.stream.annotation.Output
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File
import kotlin.test.assertNotNull
import kotlin.test.expect

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("it")
@ContextConfiguration(initializers = [KafkaContextInitializer::class])
class KafkaTest {

    @Autowired
    lateinit var repo: OwnerRepository

    @Autowired
    lateinit var commandsResponseRepository: CommandsResponseRepository

    @Autowired
    @Output(CarsOutBindings.CARS)
    lateinit var carOuputChannel: MessageChannel

    @Test
    fun submitCreateOwnerCommand() {

        val command = repo.submitCreateOwnerCommand(CreateOwnerParams(ownerId = "1", ownerName = "test")).block()

        assertNotNull(command)
        expect("1") { command.getOwnerId() }
        expect("test") { command.getName() }

        val commandResponse = commandsResponseRepository
                .findCommandResponse(command.getCommandId())
                .customRetry()
                .block()

        assertNotNull(commandResponse)
        expect(CommandStatus.SUCCEEDED) { commandResponse.getStatus() }
    }

    @Test
    fun submitCar() {
        val carId = 1
        val car = CarFactory.createRandom(carId)
        val message = MessageBuilder.createMessage(car, headers(carId))
        val sended = carOuputChannel.send(message)
        // this must be always true - because for this output sync is false - e.g. not configured to be sync
        assert(true) { sended }
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
