@file:Suppress("UnstableApiUsage")
rootProject.name = "cats_town"
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
    versionCatalogs {
        create("libs") {
            version("androidxCore", "1.10.0")
            version("androidxFragment", "1.3.6")
            version("recyclerView", "1.3.2")
            version("timber", "4.7.1")
            version("appCompat", "1.6.1")

            library("androidx-appcompat", "androidx.appcompat", "appcompat").versionRef("appCompat")
            library("androidx-fragment", "androidx.fragment", "fragment").versionRef("androidxFragment")
            library("androidx-core", "androidx.core", "core-ktx").versionRef("androidxCore")
            library("recyclerview", "androidx.recyclerview", "recyclerview").versionRef("recyclerView")
            library("timber", "com.jakewharton.timber", "timber").versionRef("timber")
            library("commons-lang3", "org.apache.commons:commons-lang3:3.9")
        }
    }
}