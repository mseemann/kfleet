package io.kfleet.simulation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class SimulatorApp

fun main(args: Array<String>) {
    runApplication<SimulatorApp>(*args)
}



