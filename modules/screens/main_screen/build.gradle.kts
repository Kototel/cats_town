plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

kotlin {
    android()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.appcompat)
            }
        }

        val commonMain by getting {
            dependencies {

            }
        }
    }
}

android {
    namespace = "com.conditional.cats_town.main_screen"
    compileSdk = extra["android.compileSdk"].toString().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/res")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}