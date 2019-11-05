plugins {
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
    id("com.commercehub.gradle.plugin.avro") version "0.17.0"
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

    implementation("org.springframework.boot:spring-boot-starter-test:2.1.9.RELEASE") {
        exclude(module = "junit")
    }

    implementation("org.testcontainers:junit-jupiter:1.12.2")
}
