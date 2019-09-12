package io.kfleet.cars.service


import io.kfleet.common.configuration.EnableKotlinJsonModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableKotlinJsonModule
class CarServiceApp

fun main(args: Array<String>) {
    runApplication<CarServiceApp>(*args)
}
