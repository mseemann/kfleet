rootProject.name = "kfleet"

//include("simulator")
//include("monitoring")
include("domain")
//include("travel-request-validator")
include("owner-service")
include("common")

// make the avro plugihn available
pluginManagement {
    repositories {
        gradlePluginPortal()
        jcenter()
        maven(url = "https://dl.bintray.com/gradle/gradle-plugins")
    }
}
