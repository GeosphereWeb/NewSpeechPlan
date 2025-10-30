// gradle/jacoco-report-aggregation.gradle.kts
import java.io.File

// Function to read exclusion patterns from a file.
fun readExclusionPatterns(file: File): List<String> {
    return if (file.exists()) {
        file.readLines().map { it.trim() }.filter { it.isNotEmpty() }
    } else {
        println("Warning: JaCoCo exclusion file not found at ${file.absolutePath}. No exclusions will be applied.")
        emptyList()
    }
}

// Configure a unified JaCoCo report for all subprojects.
// This task will be added to the root project.
tasks.register<JacocoReport>("jacocoAggregatedReport") {
    description = "Generates a unified JaCoCo code coverage report across all modules."

    // Specify the projects whose test results should be included.
    val projectsToInclude = subprojects.filter {
        it.pluginManager.hasPlugin("com.android.application") ||
        it.pluginManager.hasPlugin("com.android.library")
    }

    // The aggregation task must run after the tests of all included projects.
    dependsOn(projectsToInclude.map { it.tasks.named("testDebugUnitTest") })

    // Read exclusion patterns from the central file.
    val exclusionFile = rootProject.file("config/jacoco/jacoco_class_exclusions.txt")
    val classExclusionPatterns = readExclusionPatterns(exclusionFile)

    // Collect sources, class files, and execution data from all included projects.
    val sourceDirs = files()
    val classDirs = files()
    val execFiles = files()

    projectsToInclude.forEach { project ->
        sourceDirs.from(
            "${project.projectDir}/src/main/java",
            "${project.projectDir}/src/main/kotlin"
        )

        classDirs.from(
            project.fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
                // Apply default and custom exclusions.
                exclude(
                    listOf(
                        "**/R.class",
                        "**/R$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*"
                    ) + classExclusionPatterns
                )
            }
        )

        execFiles.from(
            project.fileTree(project.buildDir) {
                include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
            }
        )
    }

    sourceDirectories.setFrom(sourceDirs)
    classDirectories.setFrom(classDirs)
    executionData.setFrom(execFiles)

    // Configure the output formats for the aggregated report.
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
