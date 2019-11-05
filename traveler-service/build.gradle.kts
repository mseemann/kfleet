plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
    id("com.commercehub.gradle.plugin.avro")
    jacoco
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    implementation(project(":common"))
    implementation(project(":domain"))
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")
    implementation("org.springframework.cloud:spring-cloud-stream-reactive")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${property("jacksonKotlinVersion")}")
    implementation("io.github.microutils:kotlin-logging:1.7.6")
    implementation("org.apache.avro:avro:1.9.1")
    implementation("io.confluent:kafka-avro-serializer:${property("confluentVersion")}")
    implementation("io.confluent:kafka-streams-avro-serde:${property("confluentVersion")}")
    implementation("io.confluent:kafka-schema-registry-client:${property("confluentVersion")}")

    testImplementation(project(":test-common"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.awaitility:awaitility:4.0.1")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.1")
    testImplementation("org.testcontainers:junit-jupiter:1.12.2")

}
