plugins {
    id("com.android.library")
    kotlin("multiplatform")
}

android {
    namespace = "com.conditional.cats_town.screens.main_screen"
    compileSdk = extra["android.compileSdk"].toString().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/res")

    defaultConfig {
        minSdk = extra["android.minSdk"].toString().toInt()
        targetSdk = extra["android.targetSdk"].toString().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

kotlin {
    android()

    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.appcompat)
                implementation(project(":custom_view_tools"))
                implementation(project(":simple_text_view"))
            }
        }

        val commonMain by getting {
            dependencies {

            }
        }
    }
}