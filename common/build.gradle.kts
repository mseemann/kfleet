plugins {
    kotlin("jvm")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.apache.kafka:kafka-streams:2.0.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux:2.1.9.RELEASE")
    implementation("org.apache.avro:avro:1.9.1")
    implementation("io.confluent:kafka-streams-avro-serde:5.2.1")
    implementation("io.confluent:kafka-avro-serializer:5.2.2")
    implementation("io.confluent:kafka-schema-registry-client:5.2.1")
}
