import com.intellij.rt.coverage.report.api.ReportApi.xmlReport
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(this.libs.plugins.android.application) apply false
    alias(this.libs.plugins.kotlin.android) apply false
    alias(this.libs.plugins.kotlin.compose) apply false
    alias(this.libs.plugins.google.services) apply false
    alias(this.libs.plugins.ktlint)
    alias(this.libs.plugins.detekt)
    alias(this.libs.plugins.sonarcube) apply true
    alias(this.libs.plugins.android.library) apply false // Überprüfe die neueste Version
    alias(this.libs.plugins.kover) // Add Kover plugin
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.

val libsWS = extensions.getByType<VersionCatalogsExtension>().named("libs")

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

        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/kover/report.xml")
        property(
            "sonar.kotlin.detekt.reportPaths",
            subprojects
                .filter { it.plugins.hasPlugin("io.gitlab.arturbosch.detekt") }
                .joinToString(",") {
                    "${it.layout.buildDirectory.get()}/reports/detekt/detekt.xml"
                }
        )
        property(
            "sonar.androidLint.reportPaths",
            subprojects
                .filter { it.plugins.hasPlugin("com.android.application") || it.plugins.hasPlugin("com.android.library") }
                .joinToString(",") {
                    "${it.layout.buildDirectory.get()}/reports/lint-results-debug.xml"
                }
        )
        property("sonar.gradle.skipCompile", "true")
        property("sonar.sources", "src/main/java,src/main/kotlin")
        property("sonar.tests", "src/test/java,src/test/kotlin,src/androidTest/kotlin,src/androidTest/java")
        property("sonar.sourceEncoding", "UTF-8")
        // property("sonar.kotlin.rules.S107.max", "10") // Erlaube bis zu 10 Parameter
    }
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        parallel = true
        // debug = true
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(true)
            sarif.required.set(true)
            md.required.set(true)
        }
    }

    detekt {
        toolVersion = libsWS.findVersion("detekt").get().toString()
        config.setFrom(file("$rootDir/config/detekt/detekt.yml"))
        source.setFrom(files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin"))
        buildUponDefaultConfig = true
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        debug.set(true)
        outputColorName.set("RED")
        enableExperimentalRules.set(true)
        reporters {
            reporter(ReporterType.CHECKSTYLE)
            reporter(ReporterType.HTML)
            reporter(ReporterType.PLAIN)
        }
    }
}

// Kover Configuration
kover {
    // useJacoco() // EMPFEHLUNG: Auskommentieren. Die Standard Kover-Engine ist für Kotlin meist präziser.
    merge {
        subprojects()
    }

    reports {
        total {
            html { onCheck = true }
            xml { onCheck = true }
        }
        // Filterung (optional, hast du schon auskommentiert)
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*_Factory",
                    "*_MembersInjector",
                    "*Hilt*",
                    "MainActivity",
                    "MainActivityKt",
                    "MyApplication"
                )
                packages("*.di", " de.geosphere.speechplaning.theme"/*"com.example.ui", "com.example.generated"*/)
                annotatedBy("kotlin.Deprecated", "androidx.compose.runtime.Composable")
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
    kover(project(":feature:congregationEvent"))
    kover(project(":feature:profile"))
    kover(project(":feature:settings"))
    kover(project(":feature:speaker"))
    kover(project(":feature:speeches"))
    kover(project(":theme"))
}

// Task zum Sammeln aller Android Lint Reports
tasks.register("collectLintReports") {
    description = "Sammelt alle Android Lint Reports aus allen Submodulen in ein zentrales Verzeichnis"
    group = "reporting"

    // Automatisch alle lintDebug Tasks als Abhängigkeiten hinzufügen
    val modules = listOf(
        "app",
        "feature:home", "feature:settings", "feature:profile", "feature:login",
        "feature:congregationEvent", "feature:speeches", "feature:districts",
        "feature:congregation", "feature:speaker",
        "core:ui", "core:model", "core:navigation",
        "data", "theme"
    )

    modules.forEach { moduleName ->
        dependsOn(":${moduleName}:lintDebug")
    }

    doLast {
        val collectDir = layout.buildDirectory.file("app-lint-reports").get().asFile
        collectDir.mkdirs()

        val allReports = StringBuilder()
        allReports.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        allReports.append("<issues>\n")

        modules.forEach { moduleName ->
            val moduleProject = rootProject.findProject(":${moduleName}")
            if (moduleProject != null) {
                val reportFile = moduleProject.layout.buildDirectory.file("reports/lint-results-debug.xml").get().asFile
                if (reportFile.exists()) {
                    try {
                        val content = reportFile.readText()
                        // Extrahiere nur die <issue>-Elemente
                        val issuePattern = Regex("<issue[^>]*>.*?</issue>", RegexOption.DOT_MATCHES_ALL)
                        val matches = issuePattern.findAll(content)
                        val issueCount = matches.count()
                        matches.forEach { match ->
                            allReports.append(match.value).append("\n")
                        }
                        println("✓ Lint Report gefunden: $moduleName ($issueCount Issues)")
                    } catch (e: Exception) {
                        println("⚠ Fehler beim Verarbeiten des Reports von $moduleName: ${e.message}")
                    }
                }
            }
        }

        allReports.append("</issues>\n")

        val mergedReport = File(collectDir, "merged-lint-report.xml")
        mergedReport.writeText(allReports.toString())
        println("✓ Zusammengefasster Lint-Report erstellt: ${mergedReport.absolutePath}")
    }
}

