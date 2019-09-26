import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

fun <T> Mono<T>.customRetry(): Mono<T> {
    return this.retryBackoff(5, Duration.ofSeconds(1), Duration.ofSeconds(3))
}

fun <T> Flux<T>.customRetry(): Flux<T> {
    return this.retryBackoff(5, Duration.ofSeconds(1), Duration.ofSeconds(3))
}
