
import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Top-level build file where you can add configuration options common to all sub-projects/modules.

val libsws = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun loadPatternsFromFile(filePath: String, descriptionForWarning: String): String {
    val exclusionFile = rootProject.file(filePath) // rootProject is available in this script's scope
    return if (exclusionFile.exists()) {
        exclusionFile.readLines().filter { it.isNotBlank() }.joinToString(",")
    } else {
        println( // Using println to match the original style
            "Warning: $descriptionForWarning file not found at ${exclusionFile.absolutePath}. " +
                "No exclusions will be applied."
        )
        "" // Fallback if the file doesn't exist or is empty
    }
}

val coverageExclusionPatterns = loadPatternsFromFile(
    "config/sonar/coverage_exclusions.txt",
    "SonarQube coverage exclusion"
)

val duplicationExclusionPatterns = loadPatternsFromFile(
    "config/sonar/duplication_exclusions.txt",
    "SonarQube duplication exclusion" // Corrected description
)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.sonarcube) apply true
    alias(libs.plugins.android.library) apply false // Überprüfe die neueste Version
    alias(libs.plugins.kover) // Add Kover plugin
}

// apply(from = "${rootDir}/gradle/jacoco-report-aggregation.gradle.kts") // Remove Jacoco script

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.ktlint.gradle)
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "GeosphereWeb_NewSpeechPlan")
        property("sonar.organization", "geosphereweb")
        property("sonar.host.url", "https://sonarcloud.io")

        property("sonar.exclusions", "**/google-services.json,another/file/to/exclude.java")
        property("sonar.sourceEncoding", "UTF-8")

        if (coverageExclusionPatterns.isNotEmpty()) {
            property("sonar.coverage.exclusions", coverageExclusionPatterns)
        }

        if (duplicationExclusionPatterns.isNotEmpty()) {
            property("sonar.cpd.exclusions", duplicationExclusionPatterns)
        }

        // property(
        //     "sonar.coverage.jacoco.xmlReportPaths",
        //     "$buildDir/reports/kover/report.xml" // Point Sonar to Kover report
        // )
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "**/build/reports/kover/report.xml,**/build/reports/kover/reportDebug.xml"
        )

        property("sonar.androidLint.reportPaths", "**/build/reports/lint-results-*.xml")

        property("sonar.gradle.skipCompile", "true")
        // property("sonar.kotlin.rules.S107.max", "10") // Erlaube bis zu 10 Parameter
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    // Kover wird automatisch vom Plugin verwaltet, wenn es auf root-Ebene angewendet wird.
    // Es sammelt Daten aus allen Modulen.

    // --- FIX: Kover für ALLE Module aktivieren, nicht nur für Android ---
    apply(plugin = "org.jetbrains.kotlinx.kover")

    detekt {
        toolVersion = libsws.findVersion("detekt").get().toString()
        config.setFrom(file("$rootDir/config/detekt/detekt.yml"))
        source.setFrom(files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin"))
        buildUponDefaultConfig = true
    }

    // Jacoco Konfiguration entfernen wir hier

    plugins.withId("org.jetbrains.kotlin.android") {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")

        ktlint {
            android.set(true)
            outputColorName.set("RED")
            ignoreFailures.set(false)
            enableExperimentalRules.set(true)
        }
        configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            debug.set(true)
            reporters {
                reporter(ReporterType.PLAIN)
                reporter(ReporterType.CHECKSTYLE)
            }
            kotlinScriptAdditionalPaths {
                include(fileTree("scripts/"))
            }
            filter {
                exclude("**/generated/**")
                include("**/kotlin/**")
            }
        }
    }

    dependencies {
        detekt(libsws.findLibrary("detekt-cli").get())
        detektPlugins(libsws.findLibrary("detekt-formatting").get())
    }
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// Kover Configuration
kover {
    // useJacoco() // EMPFEHLUNG: Auskommentieren. Die Standard Kover-Engine ist für Kotlin meist präziser.

    reports {
        // Filterung (optional, hast du schon auskommentiert)
        filters {
            excludes {
                classes("*.BuildConfig", "*_Factory", "*_MembersInjector", "*Hilt*")
            }
        }

        // 3. Verifikation (Test schlägt fehl, wenn < 50%)
        verify {
            rule {
                minBound(50)
            }
        }
    }
}

dependencies {
    kover(project(":app"))
    kover(project(":core:model"))
    kover(project(":core:navigation"))
    kover(project(":core:ui"))
    kover(project(":data"))
    kover(project(":feature:congregation"))
    kover(project(":feature:home"))
    kover(project(":feature:login"))
    kover(project(":feature:planning"))
    kover(project(":feature:profile"))
    kover(project(":feature:settings"))
    kover(project(":feature:speaker"))
    kover(project(":feature:speeches"))
    kover(project(":mocking"))
    kover(project(":theme"))
}
