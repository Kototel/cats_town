plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.conditional.cats_town"

    defaultConfig {
        applicationId = "com.conditional.cats_town"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.appcompat)

    implementation(project(":modules:screens:main_screen"))
    implementation(project(":modules:utils:custom_view_tools"))
}