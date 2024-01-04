plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.conditional.cats_town"
    compileSdk = extra["android.compileSdk"].toString().toInt()

    defaultConfig {
        applicationId = "com.conditional.cats_town"
        minSdk = extra["android.minSdk"].toString().toInt()
        targetSdk = extra["android.targetSdk"].toString().toInt()
        versionCode = extra["app.versionCode"].toString().toInt()
        versionName = extra["app.versionName"].toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.appcompat)

    implementation(project(":main_screen"))
}