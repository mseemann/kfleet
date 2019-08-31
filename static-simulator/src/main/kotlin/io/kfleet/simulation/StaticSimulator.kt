package io.kfleet.simulation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class StaticSimulator

fun main(args: Array<String>) {
    runApplication<StaticSimulator>(*args)
}



