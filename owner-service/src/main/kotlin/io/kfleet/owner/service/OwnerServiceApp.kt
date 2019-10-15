package io.kfleet.owner.service


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kfleet.common.WebClientUtil
import io.kfleet.common.configuration.MixInIgnoreAvroSchemaProperties
import org.apache.avro.specific.SpecificRecord
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.reactive.config.WebFluxConfigurer


@SpringBootApplication
@Import(WebClientUtil::class)
class OwnerServiceApp

fun main(args: Array<String>) {
    runApplication<OwnerServiceApp>(*args)
}


@Configuration
@EnableWebFlux
class WebConfiguration : WebFluxConfigurer {

    override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
        val mapper = jacksonObjectMapper()
        mapper.addMixIn(SpecificRecord::class.java, MixInIgnoreAvroSchemaProperties::class.java)
        configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(mapper))
    }
}
