import com.google.cloud.tools.jib.gradle.JibExtension
import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.utils.addIfNotNull

plugins {
    base
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.21" apply false
    id("org.springframework.boot") version "2.1.9.RELEASE" apply false
    id("com.commercehub.gradle.plugin.avro") version "0.17.0" apply false
    id("com.google.cloud.tools.jib") version "1.8.0" apply false
    jacoco
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
extra["springCloudVersion"] = "Greenwich.SR2"
extra["confluentVersion"] = "5.2.3"
extra["jacksonKotlinVersion"] = "2.9.9"
extra["testconatinerVersion"] = "1.15.2"

allprojects {

    apply(plugin = "jacoco")

    group = "org.gradle.kotlin.dsl.samples.multiproject"

    version = "1.0"

    repositories {
        jcenter()
        maven(url = "http://packages.confluent.io/maven/")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

}

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")

    the<DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        }
    }

    dependencies {
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
        testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
        testImplementation("io.mockk:mockk:1.9.3.kotlin12")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.1")
    }


    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
            xml.destination = file("${buildDir}/reports/jacoco/report.xml")
            csv.isEnabled = false
            html.isEnabled = true
            html.destination = file("$buildDir/reports/coverage")
        }
    }

}

val tagSet = mutableSetOf("latest")
tagSet.addIfNotNull(System.getenv("CIRCLE_BRANCH"))
tagSet.addIfNotNull(System.getenv("CIRCLE_BUILD_NUM"))

subprojects.forEach {
    it.afterEvaluate {
        // this is true if the jib plugin is applied to the project - but only after
        // the gradle project is evaluated
        if (it.hasProperty("jib")) {
            (it.property("jib") as JibExtension).apply {
                from {
                    image = "openjdk:8-jdk-alpine"
                }
                to {
                    image = "seemann/kfleet.io.${it.name}"
                    tags = tagSet
                }
                container {
                    jvmFlags = listOf("-Xms512m")
                    useCurrentTimestamp = true
                }
            }
        }
    }
}


jacoco {
    toolVersion = "0.8.5"
}

