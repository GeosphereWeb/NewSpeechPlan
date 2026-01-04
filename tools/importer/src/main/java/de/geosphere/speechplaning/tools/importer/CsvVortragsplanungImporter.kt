package de.geosphere.speechplaning.tools.importer

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.annotation.Exclude
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class Event {
    CIRCUIT_ASSEMBLY,
    CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER,
    CIRCUIT_OVERSEER_CONGREGATION_VISIT, // Dienstwoche
    CONVENTION, // Regionaler Kongress
    MEMORIAL, // Gedächnismal
    SPECIAL_LECTURE, // Sondervortrag

    BRANCH_CONVENTION,
    MISCELLANEOUS,
    UNKNOWN,
}

// Data Class for the Congregation Event (updated)
data class CongregationEvent(
    @get:Exclude val id: String = "",
    val dateString: String? = null, // For Firestore
    val eventType: Event = Event.CONVENTION,

    val speechId: String? = null,
    val speechNumber: String? = null,
    val speechSubject: String? = null,

    val speakerId: String? = null,
    val speakerName: String? = null,

    val speakerCongregationId: String? = null,
    val speakerCongregationName: String? = null,

    val notes: String? = null,
) {
    @get:Exclude
    val date: LocalDate?
        get() = dateString?.let { LocalDate.parse(it) }
}

// Main entry point for the planning import
fun main() = runBlocking {
    val importer = CsvVortragsplanungImporter()
    // Path to Service Account JSON file - PLEASE ADJUST!
    val serviceAccountPath = "C:/Users/werne/AndroidStudioProjects/NewSpeechPlan/serviceAccountKey.json"

    // Path to CSV file - PLEASE ADJUST!
    val csvFilePath = "Export_Vortragsplanung.CSV"

    importer.importData(serviceAccountPath, csvFilePath)
}

@Suppress("MagicNumber")
class CsvVortragsplanungImporter {

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
        // Name des Parameters sollte hier auch angepasst werden zu resourcePath
        val lines = try {
            // Hole den ClassLoader, um auf interne Ressourcen zuzugreifen
            val inputStream = this.javaClass.classLoader.getResourceAsStream(csvFilePath)
                ?: throw IllegalArgumentException("Resource not found in classpath: $csvFilePath")

            // Lese den InputStream mit dem korrekten Zeichensatz
            inputStream.bufferedReader(Charsets.UTF_8).readLines().drop(1)
        } catch (e: Exception) {
            println("Error reading CSV from resources: ${e.message}")
            return
        }

        println("Found ${lines.size} events to import.")

        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        var successCount = 0
        var failureCount = 0

        lines.forEach { line ->
            val columns = line.split(';')

            if (columns.size < 13) {
                println("Skipped row (too few columns): $line")
                failureCount++
                return@forEach
            }

            try {
                // --- Map CSV columns to new CongregationEvent structure ---

                val dateStrRaw = columns.getOrElse(0) { "" }.trim()
                val dateString = LocalDate.parse(dateStrRaw, formatter).toString() // Format to YYYY-MM-DD

                val weekEvent = columns.getOrElse(1) { "" }.trim().toInt()
                val eventType = when (weekEvent) {
                    3 -> Event.CIRCUIT_ASSEMBLY
                    // Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER
                    2 -> Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT
                    4 -> Event.CONVENTION
                    5 -> Event.MEMORIAL
                    7 -> Event.SPECIAL_LECTURE

                    6 -> Event.BRANCH_CONVENTION
                    1 -> Event.MISCELLANEOUS
                    else -> Event.UNKNOWN
                }

                val speechNumber = columns.getOrElse(11) { "" }.trim().ifBlank { null }
                val speakerName = "${columns.getOrElse(5) { "" }.trim()} ${columns.getOrElse(4) { "" }.trim()}".trim()

                val event = CongregationEvent(
                    dateString = dateString,
                    eventType = eventType,
                    speechId = speechNumber,
                    speechNumber = speechNumber,
                    speechSubject = columns.getOrElse(12) { "" }.trim().ifBlank { null },
                    speakerId = columns.getOrElse(3) { "" }.trim().ifBlank { null },
                    speakerName = if (speakerName.isNotBlank()) speakerName else null,
                    speakerCongregationId = columns.getOrElse(9) { "" }.trim().ifBlank { null },
                    speakerCongregationName = columns.getOrElse(10) { "" }.trim().ifBlank { null },
                    notes = null
                )

                // Write each event to the top-level "congregationEvents" collection
                db.collection("congregationEvents").document(dateString).set(event).get()
                successCount++
            } catch (e: Exception) {
                println("Error processing row: $line")
                println("Error: ${e.message}")
                failureCount++
            }
        }

        println("----------------------------------------------------")
        println("Import of planning data completed!")
        println("$successCount of ${lines.size} events imported successfully.")
        if (failureCount > 0) {
            println("$failureCount events failed to import.")
        }
        println("----------------------------------------------------")
    }
}
