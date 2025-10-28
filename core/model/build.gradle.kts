import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("jacoco")
}

android {
    namespace = "de.geosphere.speechplaning.core.model"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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

dependencies {
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)

    // Import Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

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

}

// Lade JaCoCo-Klassenausschlüsse aus einer externen Datei
val jacocoExclusionFile = rootProject.file("config/jacoco/jacoco_class_exclusions.txt")
val jacocoExclusionPatterns = if (jacocoExclusionFile.exists()) {
    jacocoExclusionFile.readLines().filter { it.isNotBlank() }
} else {
    println(
        "Warning: JaCoCo class exclusion file not found at ${jacocoExclusionFile.absolutePath}. " +
            "No class exclusions will be applied."
    )
    emptyList<String>() // Fallback, falls die Datei nicht existiert oder leer ist
}

// Erstellt den Task, den wir im Workflow aufrufen werden.
tasks.register<JacocoReport>("jacocoTestReport") {
    // Dieser Task hängt von den normalen Unit-Tests ab.
    description = "Generate Jacoco coverage reports after running tests."
    group = JavaBasePlugin.BUILD_TASK_NAME
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    // --- Korrektur 1: classDirectories ---
    val classDirsProvider = layout.buildDirectory.dir("tmp/kotlin-classes/debug")
    classDirectories.from(
        files(classDirsProvider).asFileTree.matching {
            exclude(jacocoExclusionPatterns)
        }
    )

    // --- Quellverzeichnisse (war schon ok) ---
    val sourceDirs = files("src/main/java", "src/main/kotlin")
    sourceDirectories.setFrom(sourceDirs)

    // --- Korrektur 2: executionData ---
    // Der Pfad zu den JaCoCo-Ausführungsdaten. Nach dem Wechsel zu Kotest/JUnit5 wird
    // die .exec-Datei möglicherweise nur noch am Standard-Gradle-JaCoCo-Plugin-Speicherort erstellt.
    // Wir verwenden nur noch diesen Pfad, um sicherzustellen, dass der Bericht gefunden wird.
    executionData.from(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"))
}
