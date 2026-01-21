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
        maven(url = "https://api.xposed.info/")
    }
}
plugins {
    id("com.autonomousapps.build-health") version "3.5.1"

    id("com.android.application") version "8.8.2" apply false
    id("org.jetbrains.kotlin.android") version "2.3.0" apply false
    id("com.google.devtools.ksp") version "2.3.4" apply false
}

rootProject.name = "Telegami"
include(":app")
