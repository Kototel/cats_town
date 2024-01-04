plugins {
    id("com.android.library")
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.20"
}

kotlin {
    android()

    sourceSets {
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.1")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.0")
            }
        }

        val commonMain by getting {
            dependencies {

            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.conditional.cats_town.main_screen"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/res")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}