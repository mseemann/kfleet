package io.kfleet.monitoring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MonitoringApp

fun main(args: Array<String>) {
    runApplication<MonitoringApp>(*args)
}
