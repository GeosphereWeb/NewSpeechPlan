plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("jacoco")
}

android {
    namespace = "de.geosphere.speechplaning.feature.profile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }

    testOptions {
        unitTests {
            all {
                it.useJUnitPlatform()
            }
        }
    }
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(project(":data"))
    implementation(project(":theme"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Unit Tests
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk) {
        exclude(group = "io.mockk", module = "mockk-android")
    }
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit) // JUnit 4 für backwards compatibility
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.property)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
