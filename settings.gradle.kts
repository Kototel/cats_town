@file:Suppress("UnstableApiUsage")
rootProject.name = "Cats Town"
apply(from = "gradle/app_modules.gradle.kts")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val agpVersion = extra["agp.version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("android").version(kotlinVersion)

        id("com.android.application").version(agpVersion)
        id("com.android.library").version(agpVersion)
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}