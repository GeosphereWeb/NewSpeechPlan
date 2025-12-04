pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
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
include(":feature:home")
include(":feature:settings")
include(":feature:profile")
include(":data")
include(":core:ui")
include(":core:model")
include(":core:navigation")
include(":mocking")
include(":theme")
include(":feature:login")
include(":feature:planning")
include(":feature:speaker")
include(":feature:speeches")
include(":feature:congregation")
include(":feature:districts")
include(":feature:districts")
