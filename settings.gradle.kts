rootProject.name = "kfleet"

include("domain")
include("common")
include("test-common")
include("owner-service")
include("car-service")
include("traveler-service")
include("ride-request-dispatcher")

// make the avro plugin available
pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven(url = "https://dl.bintray.com/gradle/gradle-plugins")
    }
}
