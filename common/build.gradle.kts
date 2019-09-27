plugins {
    kotlin("jvm")
    id("org.springframework.boot") version "2.1.7.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("plugin.spring") version "1.3.50"
}


java.sourceCompatibility = JavaVersion.VERSION_1_8

extra["springCloudVersion"] = "Greenwich.SR2"

repositories {
    jcenter()
    maven(url = "http://packages.confluent.io/maven/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    implementation("org.springframework.cloud:spring-cloud-starter-stream-kafka")
    implementation("org.apache.kafka:kafka-streams:2.0.1")
    implementation("org.springframework.boot:spring-boot-starter-webflux:2.1.7.RELEASE")
    implementation("org.apache.avro:avro:1.9.1")
    implementation("io.confluent:kafka-streams-avro-serde:5.2.1")
    implementation("io.confluent:kafka-avro-serializer:5.2.2")
    implementation("io.confluent:kafka-schema-registry-client:5.2.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
