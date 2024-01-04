@file:Suppress("UnstableApiUsage")
rootProject.name = "cats_town"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"].toString()
        val agpVersion = extra["agp.version"].toString()

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
            version("commonsLang", "3.9")
            version("androidxAnnotation", "1.3.0")

            library("androidx-appcompat", "androidx.appcompat", "appcompat").versionRef("appCompat")
            library("androidx-fragment", "androidx.fragment", "fragment").versionRef("androidxFragment")
            library("androidx-core", "androidx.core", "core-ktx").versionRef("androidxCore")
            library("androidx-annotation", "androidx.annotation", "annotation").versionRef("androidxAnnotation")
            library("recyclerview", "androidx.recyclerview", "recyclerview").versionRef("recyclerView")
            library("timber", "com.jakewharton.timber", "timber").versionRef("timber")
            library("commons-lang3", "org.apache.commons", "commons-lang3").versionRef("commonsLang")
        }
    }
}

val screens = "modules/screens"
val utils = "modules/utils"
val design = "modules/design"

include(":android")
project(":android").projectDir = file("apps/android")

// region Screens
include(":main_screen")
project(":main_screen").projectDir = file("$screens/main_screen")
// endregion

// region Utils
include(":custom_view_tools")
project(":custom_view_tools").projectDir = file("$utils/custom_view_tools")
// endregion

// region Design
include(":simple_text_view")
project(":simple_text_view").projectDir = file("$design/simple_text_view")
// endregion