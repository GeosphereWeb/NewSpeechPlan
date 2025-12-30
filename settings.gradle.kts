pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "New Speech Plan"
include(":app")
include(":data")
include(":theme")
include(":core:model")
include(":core:ui")
include(":feature:login")
include(":feature:home")
include(":core:navigation")
include(":feature:congregation")
include(":feature:profile")
include(":feature:congregationEvent")
include(":feature:settings")
include(":feature:speaker")
include(":feature:speeches")
include(":feature:districts")
include(":tools:importer")
