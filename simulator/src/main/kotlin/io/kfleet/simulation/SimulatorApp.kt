package io.kfleet.simulation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


@SpringBootApplication
@EnableAsync
class SimulatorApp

fun main(args: Array<String>) {
    runApplication<SimulatorApp>(*args)
}



