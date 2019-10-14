rootProject.name = "kfleet"

include("domain")
include("common")
include("test-common")
include("owner-service")
include("car-service")

// make the avro plugihn available
pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven(url = "https://dl.bintray.com/gradle/gradle-plugins")
    }
}
