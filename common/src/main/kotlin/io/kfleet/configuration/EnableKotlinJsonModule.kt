package io.kfleet.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
@Import(ObjectMapperConfig::class)
@Configuration
annotation class EnableKotlinJsonModule {
}
