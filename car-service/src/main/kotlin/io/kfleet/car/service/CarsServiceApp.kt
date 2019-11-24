package io.kfleet.car.service


import io.kfleet.common.WebClientUtil
import io.kfleet.common.configuration.EnableKotlinJsonModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(WebClientUtil::class)
@EnableKotlinJsonModule
class CarServiceApp

fun main(args: Array<String>) {
    runApplication<CarServiceApp>(*args)
}

