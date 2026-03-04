pluginManagement {
    repositories {
        google()
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

rootProject.name = "RedScreenFilter"
include(":app")
include(":core:designsystem")
include(":core:model")
include(":data:preferences")
include(":data:analytics")
include(":feature:settings")
include(":feature:analytics")
include(":feature:app_exemption")
