package de.geosphere.speechplaning.tools.importer

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.FileInputStream

// Data Class for a Speech
data class Speech1(
    val number: String = "",
    val subject: String = "",
    val active: Boolean = true,
)

// Main entry point for the speech import
fun main() = runBlocking {
    val importer = CsvSpeechesImporter()
    // Path to Service Account JSON file - PLEASE ADJUST!
    val serviceAccountPath = "C:/Users/werne/AndroidStudioProjects/NewSpeechPlan/serviceAccountKey.json"

    // Path to CSV file - PLEASE ADJUST!
    val csvFilePath = "NEU_tblVortaege.csv"

    importer.importData(serviceAccountPath, csvFilePath)
}

class CsvSpeechesImporter {

    @Suppress("LongMethod", "TooGenericExceptionCaught")
    suspend fun importData(serviceAccountPath: String, csvFilePath: String) {
        // --- 1. Initialize Firebase Admin SDK ---
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = withContext(Dispatchers.IO) {
                FileInputStream(serviceAccountPath)
            }
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()
            FirebaseApp.initializeApp(options)
            println("Firebase Admin SDK initialized.")
        }

        val db = FirestoreClient.getFirestore()

        // --- 2. Read CSV ---
        println("Reading CSV from resource: $csvFilePath")
        val lines = try {
            val inputStream = this.javaClass.classLoader.getResourceAsStream(csvFilePath)
                ?: throw IllegalArgumentException("Resource not found in classpath: $csvFilePath")

            inputStream.bufferedReader(Charsets.UTF_8).readLines().drop(1)
        } catch (e: Exception) {
            println("Error reading CSV from resources: ${e.message}")
            return
        }

        println("Found ${lines.size} speeches to import.")

        var successCount = 0
        var failureCount = 0

        lines.forEach { line ->
            val columns = line.split(';')

            if (columns.size < 3) {
                println("Skipped row (too few columns): $line")
                failureCount++
                return@forEach
            }

            try {
                // --- Map CSV columns to new Speech structure ---
                val speechNumber = columns.getOrElse(0) { "" }.trim()
                val title = columns.getOrElse(1) { "" }.trim()
                val isActive = columns.getOrElse(2) { "" }.trim().equals("WAHR", ignoreCase = true)

                val speech = Speech1(
                    number = speechNumber,
                    subject = title,
                    active = isActive,
                )

                // Write each speech to the "speeches" collection
                db.collection("speeches").document(speechNumber).set(speech).get()
                successCount++
            } catch (e: Exception) {
                println("Error processing row: $line")
                println("Error: ${e.message}")
                failureCount++
            }
        }

        println("----------------------------------------------------")
        println("Import of speech data completed!")
        println("$successCount of ${lines.size} speeches imported successfully.")
        if (failureCount > 0) {
            println("$failureCount speeches failed to import.")
        }
        println("----------------------------------------------------")
    }
}
