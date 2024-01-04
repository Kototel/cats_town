plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.conditional.cats_town.design.simple_text_view"
    compileSdk = extra["android.compileSdk"].toString().toInt()

    defaultConfig {
        minSdk = extra["android.minSdk"].toString().toInt()
        targetSdk = extra["android.targetSdk"].toString().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.commons.lang3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(project(":custom_view_tools"))
}