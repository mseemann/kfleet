plugins {
    id("org.springframework.boot") version "2.1.9.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("plugin.spring") version "1.3.50"
    id("com.commercehub.gradle.plugin.avro") version "0.17.0"
    jacoco
}

java.sourceCompatibility = JavaVersion.VERSION_1_8


dependencies {

    implementation(project(":common"))
    implementation(project(":domain"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.kafka:kafka-streams")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-streams")
    implementation("org.springframework.cloud:spring-cloud-stream-reactive")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    implementation("io.github.microutils:kotlin-logging:1.7.6")
    implementation("org.apache.avro:avro:1.9.1")
    implementation("io.confluent:kafka-avro-serializer:5.2.2")
    implementation("io.confluent:kafka-streams-avro-serde:5.2.1")
    implementation("io.confluent:kafka-schema-registry-client:5.2.1")

    testImplementation(project(":test-common"))
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "junit")
    }

    testImplementation("io.projectreactor:reactor-test:3.2.11.RELEASE")
    testImplementation("org.awaitility:awaitility:4.0.1")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.1")
    testImplementation("org.testcontainers:junit-jupiter:1.12.2")

}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
