plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("jacoco")
}

android {
    namespace = "de.geosphere.speechplaning.data"
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
            isIncludeAndroidResources = true
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
    implementation(project(":core:model"))

    implementation(libs.androidx.core.ktx)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth) // Firebase Authentication Email u. Passwort
    implementation(libs.play.services.auth) // Google Sign-In Client
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.firestore.ktx)

    implementation(libs.kotlinx.coroutines.play.services)

    // // Koin
    // // dependencies with Koin
    implementation(libs.koin.android)

    // Unit Tests
    testImplementation(kotlin("test"))
    testImplementation(libs.mockk) {
        exclude(group = "io.mockk", module = "mockk-android")
    }
    // testImplementation(libs.mockk.android) // Remove, not needed for pure unit tests
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit) // JUnit 4 für backwards compatibility
    testImplementation(libs.kotest.framework)
    testImplementation(libs.kotest.assertions)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.property)
    testImplementation(libs.turbine)
}
