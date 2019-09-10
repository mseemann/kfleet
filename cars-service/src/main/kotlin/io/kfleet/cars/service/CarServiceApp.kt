package io.kfleet.cars.service

import io.kfleet.configuration.EnableKotlinJsonModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableKotlinJsonModule
class CarServiceApp

fun main(args: Array<String>) {
    runApplication<CarServiceApp>(*args)
}



