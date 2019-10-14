package io.kfleet.owner.service


import io.kfleet.common.WebClientUtil
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(WebClientUtil::class)
class OwnerServiceApp

fun main(args: Array<String>) {
    runApplication<OwnerServiceApp>(*args)
}

