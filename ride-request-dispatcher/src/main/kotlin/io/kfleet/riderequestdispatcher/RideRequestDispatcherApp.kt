package io.kfleet.riderequestdispatcher

import io.kfleet.common.WebClientUtil
import io.kfleet.common.configuration.EnableKotlinJsonModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(WebClientUtil::class)
@EnableKotlinJsonModule
class RideRequestDispatcherApp

fun main(args: Array<String>) {
    runApplication<RideRequestDispatcherApp>(*args)
}

