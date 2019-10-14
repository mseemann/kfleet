plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.commercehub.gradle.plugin.avro") version "0.17.0"
    jacoco
}


java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
    maven(url = "http://packages.confluent.io/maven/")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    implementation("org.apache.avro:avro:1.9.1")
    implementation("org.apache.kafka:kafka-streams:2.0.1")
}

avro {

}
