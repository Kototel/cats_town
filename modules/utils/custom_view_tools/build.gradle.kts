plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.conditional.cats_town.custom_view_tools"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.commons.lang3)
    implementation(libs.timber)
    implementation(libs.recyclerview)
}