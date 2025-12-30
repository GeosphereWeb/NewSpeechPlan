import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    // alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlin.serialization)
    id("jacoco")
}

android {
    namespace = "de.geosphere.speechplaning"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "de.geosphere.speechplaning"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = 34
        versionCode = 1
        versionName = "0.0.1_alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false // Usually false for debug builds
            enableUnitTestCoverage = true
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes.add("META-INF/LICENSE.md")
            excludes.add("META-INF/LICENSE-notice.md")
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
    compilerOptions {
        freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    implementation(project(":feature:home"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:login"))
    implementation(project(":feature:congregationEvent"))
    implementation(project(":feature:speeches"))
    implementation(project(":feature:districts"))
    implementation(project(":feature:congregation"))
    implementation(project(":feature:speaker"))
    implementation(project(":core:ui"))
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(project(":data"))
    implementation(project(":theme"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.ui.graphics)

    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.gson)

    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // Add the dependencies for any other desired Firebase products
    // https://firebase.google.com/docs/android/setup#available-libraries
    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.auth)

    // implementation(libs.kotlinx.coroutines.play.services)

    // // Koin
    // // dependencies with Koin
    implementation(libs.koin.android) // Oder die neueste Version
    implementation(libs.koin.androidx.compose)
    implementation(libs.org.jacoco.core)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.ui.test.junit4.android) // Für

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

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.mockk.agent.jvm)

    androidTestImplementation(libs.kotest.framework)
    androidTestImplementation(libs.kotest.assertions)
    androidTestImplementation(libs.kotest.runner.junit5)
    androidTestImplementation(libs.kotest.property)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // MockK für Compose Previews verfügbar machen
    debugImplementation(libs.mockk.android)
}

// INHALT FÜR DIE DUMMY google-services.json
// WICHTIG: Dieser Inhalt muss EXAKT dem Inhalt der Datei entsprechen,
// die du in 'app/google-services.json' in Git eingecheckt hast!
val dummyJsonContent = """
{
  "project_info": {
    "project_number": "0",
    "firebase_url": "https://dummy.firebaseio.com",
    "project_id": "dummy-project",
    "storage_bucket": "dummy-project.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:0:android:0",
        "android_client_info": {
          "package_name": "de.geosphere.speechplaning"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "dummy_api_key"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
""".trimIndent()

// Pfad zur ECHTEN, lokalen google-services.json im Projekt-Root (NICHT IN GIT)
val realLocalGoogleServices = rootProject.file("google-services.json")
// Pfad zur google-services.json im app-Modul (diese ist mit DUMMY-Inhalt in GIT)
val targetAppGoogleServices = project.file("google-services.json")

// Task, um temporär die ECHTE google-services.json zu verwenden, falls lokal vorhanden
val useRealGoogleServicesTask = tasks.register("useRealGoogleServicesForLocalBuild") {
    description = "Task, um temporär die ECHTE google-services.json zu verwenden, falls lokal vorhanden"
    group = "build"
    outputs.upToDateWhen { false }
    onlyIf { realLocalGoogleServices.exists() } // Nur ausführen, wenn die ECHTE Datei im Projekt-Root existiert
    doLast {
        if (realLocalGoogleServices.exists()) {
            println(
                "LOKALER BUILD: Kopiere temporär die ECHTE google-services.json aus dem Projekt-Root in das " +
                    "app-Modul."
            )
            println("Diese wird nach dem Build durch die DUMMY-Version ersetzt.")
            realLocalGoogleServices.copyTo(targetAppGoogleServices, overwrite = true)
        }
        // Wenn die echte Datei nicht existiert (z.B. frischer Checkout ohne lokale Einrichtung, oder CI),
        // wird die bereits im app-Modul eingecheckte (Dummy-)Datei verwendet.
        // Die CI-Pipeline überschreibt diese ohnehin mit dem Inhalt aus dem Secret.
    }
}

// Task, um die DUMMY google-services.json im app-Modul wiederherzustellen
val restoreDummyGoogleServicesTask = tasks.register("restoreDummyGoogleServicesInAppModule") {
    description = "Task, um die DUMMY google-services.json im app-Modul wiederherzustellen"
    group = "build"

    // Diese Zeile sorgt dafür, dass der Task immer ausgeführt wird,
    // indem seine Outputs als niemals aktuell deklariert werden.
    outputs.upToDateWhen { false }

    doLast {
        // Stelle sicher, dass dummyJsonContent nicht leer ist (einfache Überprüfung)
        if (dummyJsonContent.isBlank()) {
            throw GradleException("Dummy JSON content is blank. Check its definition.")
        }

        // Schreibe den dummyJsonContent in die targetAppGoogleServices Datei
        targetAppGoogleServices.writeText(dummyJsonContent)
        println(
            "INFO: Task '$name' explicitly overwrote '${targetAppGoogleServices.absolutePath}'" +
                " with predefined dummy content."
        )
    }
}

// WICHTIG: Stelle sicher, dass diese Imports am Anfang deiner build.gradle.kts-Datei stehen.
// Sie sind bereits in deiner Datei vorhanden, dies dient nur zur Überprüfung.
// import com.android.build.api.variant.AndroidTest
// import com.android.build.api.variant.UnitTest
// import java.util.Locale

androidComponents {
    // 1. Konfiguriere die App-Varianten (z.B. debug, release)
    onVariants { variant ->
        // Macht aus "debug" -> "Debug"
        val capitalName = variant.name.replaceFirstChar { it.titlecase(Locale.getDefault()) }

        // --- Application Tasks ---
        tasks.findByName("process${capitalName}GoogleServices")?.dependsOn(useRealGoogleServicesTask)
        tasks.findByName("assemble${capitalName}")?.finalizedBy(restoreDummyGoogleServicesTask)
    }

    // 2. Konfiguriere die Unit-Test-Komponenten
    onVariants { variant ->
        val capitalName = variant.name.replaceFirstChar { it.titlecase(Locale.getDefault()) }

        // Finde den passenden Test-Task (z.B. "testDebugUnitTest")
        tasks.findByName("test${capitalName}UnitTest")?.let { testTask ->
            testTask.finalizedBy(restoreDummyGoogleServicesTask)
            testTask.dependsOn(useRealGoogleServicesTask)
        }
    }

    // 3. Konfiguriere die Android-Test-Komponenten (Instrumented Tests)
    onVariants { variant ->
        val capitalName = variant.name.replaceFirstChar { it.titlecase(Locale.getDefault()) }

        // Task für Google-Services des Instrumented Tests
        tasks.findByName("process${capitalName}AndroidTestGoogleServices")?.dependsOn(useRealGoogleServicesTask)
        // Task zum Zusammenbauen des Instrumented Tests
        tasks.findByName("assemble${capitalName}AndroidTest")?.finalizedBy(restoreDummyGoogleServicesTask)
    }
}
