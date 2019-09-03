package io.kfleet.dispatcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DispatcherApplication

fun main(args: Array<String>) {
    runApplication<DispatcherApplication>(*args)
}
