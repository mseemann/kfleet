plugins {
    id("org.springframework.boot") version "2.1.7.RELEASE" apply false
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("plugin.spring") version "1.3.50"
    id("com.commercehub.gradle.plugin.avro") version "0.17.0"
}


java.sourceCompatibility = JavaVersion.VERSION_1_8

extra["springCloudVersion"] = "Greenwich.SR2"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")

    implementation("org.springframework.boot:spring-boot-starter-test:2.1.7.RELEASE") {
        exclude(module = "junit")
    }

    implementation("org.testcontainers:junit-jupiter:1.12.2")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
