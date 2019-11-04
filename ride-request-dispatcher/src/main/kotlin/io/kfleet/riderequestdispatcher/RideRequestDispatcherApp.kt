package io.kfleet.riderequestdispatcher

import io.kfleet.common.WebClientUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(WebClientUtil::class)
class RideRequestDispatcherApp

fun main(args: Array<String>) {
    runApplication<RideRequestDispatcherApp>(*args)
}

