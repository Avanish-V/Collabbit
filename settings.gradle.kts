pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Finder"
include(":app")
include(":baselineprofile")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:firebase")
include(":core:designsystem")
include(":core:navigation")
include(":feature:chat")
include(":feature:feed")
include(":feature:profile")
include(":feature:auth")
include(":feature:collab")
include(":feature:explore_swipe")
include(":feature:notification")
include(":feature:opportunities")
include(":feature:reply")
include(":feature:society")
include(":core:notifications")
