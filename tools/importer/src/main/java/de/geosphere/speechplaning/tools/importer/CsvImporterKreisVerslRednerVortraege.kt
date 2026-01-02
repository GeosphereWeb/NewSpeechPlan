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
import java.nio.charset.Charset

// --- Lokale Kopien der Data Classes (um Abhängigkeiten zu Android zu vermeiden) ---

enum class SpiritualStatus {
    UNKNOWN,
    MINISTERIAL_SERVANT,
    ELDER,
}

data class District(
    @get:Exclude val id: String = "",
    val name: String = "",
    val circuitOverseerId: String = "",
    val districtLeaderId: String = "",
    val districtLeaderCongregationId: String = "",
    val active: Boolean = true,
)

data class Congregation(
    @get:Exclude val id: String = "",
    val districtId: String = "",
    val name: String = "",
    val address: String = "",
    val meetingTime: String = "",
    val active: Boolean = true,
)

data class Speaker(
    @get:Exclude val id: String = "",
    val districtId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String = "",
    val phone: String = "",
    val email: String = "",
    val spiritualStatus: SpiritualStatus,
    val speechNumberIds: List<Int> = emptyList(),
    val congregationId: String = "",
    val active: Boolean = true,
)

data class Speech(
    @get:Exclude val id: String = "",
    val number: String = "",
    val subject: String = "",
    val active: Boolean = true,
)

// --- Ende Data Classes ---

fun main() = runBlocking {
    val importer = CsvImporter()
    // Pfad zur Service Account JSON Datei - BITTE ANPASSEN!
    val serviceAccountPath = "C:/Users/werne/AndroidStudioProjects/NewSpeechPlan/serviceAccountKey.json"

    // Pfad zur CSV Datei - BITTE ANPASSEN!
    val csvFilePath = "Export_Kreis-Versl-Redner-Vortraege.CSV"

    importer.importData(serviceAccountPath, csvFilePath)
}

@Suppress("MagicNumber")
class CsvImporter {

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    suspend fun importData(serviceAccountPath: String, csvFilePath: String) {
        // --- 1. Firebase Admin SDK initialisieren ---
        if (FirebaseApp.getApps().isEmpty()) {
            val serviceAccount = withContext(Dispatchers.IO) {
                FileInputStream(serviceAccountPath)
            }

            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build()

            FirebaseApp.initializeApp(options)
            println("Firebase Admin SDK initialisiert.")
        }

        val db = FirestoreClient.getFirestore()

        // --- 2. CSV Lesen ---
        println("Lese CSV Datei: $csvFilePath")
        val lines = try {
            File(csvFilePath).readLines(Charsets.UTF_8).drop(1)
        } catch (e: Exception) {
            println("Fehler beim Lesen der CSV: ${e.message}")
            return
        }

        val districts = mutableMapOf<String, District>()
        val congregations = mutableMapOf<String, Congregation>()
        val speakers = mutableMapOf<String, Speaker>()
        val speeches = mutableMapOf<String, Speech>()
        val speakerSpeechMap = mutableMapOf<String, MutableList<String>>()

        lines.forEach { line ->
            val columns = line.split(';')

            if (columns.size < 20) return@forEach

            // --- Kreis (District) verarbeiten ---
            val districtId = columns.getOrElse(0) { "" }.trim()
            if (districtId.isNotBlank() && !districts.containsKey(districtId)) {
                districts[districtId] = District(
                    id = districtId,
                    name = columns.getOrElse(1) { "" }.trim()
                )
            }

            // --- Versammlung (Congregation) verarbeiten ---
            val congregationId = columns.getOrElse(2) { "" }.trim()
            if (congregationId.isNotBlank() && !congregations.containsKey(congregationId)) {
                val address =
                    "${
                        columns.getOrElse(
                            4
                        ) { "" }.trim()
                    } ${
                        columns.getOrElse(
                            5
                        ) { "" }.trim()
                    } ${
                        columns.getOrElse(
                            6
                        ) { "" }.trim()
                    }, ${
                        columns.getOrElse(
                            7
                        ) { "" }.trim()
                    } ${columns.getOrElse(8) { "" }.trim()}".trim()
                congregations[congregationId] = Congregation(
                    id = congregationId,
                    name = columns.getOrElse(3) { "" }.trim(),
                    districtId = districtId,
                    address = address.replace(Regex(" ,$"), "").trim(),
                    active = columns.getOrElse(11) { "" }.trim().equals("WAHR", ignoreCase = true)
                )
            }

            // --- Redner (Speaker) verarbeiten ---
            val speakerId = columns.getOrElse(12) { "" }.trim()
            if (speakerId.isNotBlank() && !speakers.containsKey(speakerId)) {
                speakers[speakerId] = Speaker(
                    id = speakerId,
                    lastName = columns.getOrElse(13) { "" }.trim(),
                    firstName = columns.getOrElse(14) { "" }.trim(),
                    mobile = columns.getOrElse(15) { "" }.trim(),
                    phone = columns.getOrElse(16) { "" }.trim(),
                    email = columns.getOrElse(17) { "" }.trim(),
                    congregationId = columns.getOrElse(18) { "" }.trim(),
                    spiritualStatus = when (columns.getOrElse(19) { "" }.trim().toIntOrNull()) {
                        1 -> SpiritualStatus.ELDER
                        2 -> SpiritualStatus.MINISTERIAL_SERVANT
                        else -> SpiritualStatus.UNKNOWN
                    },
                    active = columns.getOrElse(20) { "" }.trim().equals("WAHR", ignoreCase = true),
                )
            }

            // --- Vortrag (Speech) verarbeiten ---
            val speechNumber = columns.getOrElse(23) { "" }.trim()
            if (speechNumber.isNotBlank() && !speeches.containsKey(speechNumber)) {
                speeches[speechNumber] = Speech(
                    id = speechNumber,
                    number = speechNumber,
                    subject = columns.getOrElse(24) { "" }.trim(),
                    active = columns.getOrElse(25) { "" }.trim().equals("WAHR", ignoreCase = true)
                )
            }

            // --- Zuordnung Redner zu Vortrag ---
            if (speakerId.isNotBlank() && speechNumber.isNotBlank()) {
                val speechList = speakerSpeechMap.getOrPut(speakerId) { mutableListOf() }
                if (!speechList.contains(speechNumber)) {
                    speechList.add(speechNumber)
                }
            }
        }

        // --- Redner mit Vortragsliste aktualisieren ---
        speakerSpeechMap.forEach { (speakerId, speechIds) ->
            speakers[speakerId]?.let { speaker ->
                speakers[speakerId] = speaker.copy(
                    speechNumberIds = speechIds.mapNotNull {
                        it.toIntOrNull() // Sicherstellen, dass es Int ist
                    }
                )
            }
        }

        // --- Daten in Firestore schreiben (Hierarchisch) ---

        println("Schreibe ${districts.size} Kreise...")
        districts.values.forEach { district ->
            // Top-Level: districts/{districtId}
            db.collection("districts").document(district.id).set(district).get()
        }

        println("Schreibe ${congregations.size} Versammlungen...")
        congregations.values.forEach { congregation ->
            if (congregation.districtId.isNotBlank()) {
                // Sub-Collection: districts/{districtId}/congregations/{congregationId}
                db.collection("districts").document(congregation.districtId)
                    .collection("congregations").document(congregation.id)
                    .set(congregation).get()
            } else {
                println("Überspringe Versammlung ohne DistrictId: ${congregation.name}")
            }
        }

        println("Schreibe ${speakers.size} Redner...")
        speakers.values.forEach { speaker ->
            // Wir müssen die DistrictId über die Versammlung ermitteln
            val congregation = congregations[speaker.congregationId]

            if (congregation != null && congregation.districtId.isNotBlank()) {
                // Optional: Wir setzen die DistrictId auch im Speaker-Dokument, damit die Daten vollständig sind
                val speakerWithDistrict = speaker.copy(districtId = congregation.districtId)

                // Sub-Collection: districts/{districtId}/congregations/{congregationId}/speakers/{speakerId}
                db.collection("districts").document(congregation.districtId)
                    .collection("congregations").document(congregation.id)
                    .collection("speakers").document(speaker.id)
                    .set(speakerWithDistrict).get()
            } else {
                println(
                    "Überspringe Redner ohne gültige Zuordnung: ${speaker.lastName}, ${speaker.firstName} " +
                        "(CongregationId: ${speaker.congregationId})"
                )
            }
        }

        // println("Schreibe ${speeches.size} Vorträge...")
        // speeches.values.forEach {
        //     db.collection("speeches").document(it.id).set(it).get()
        // }

        println("Import erfolgreich abgeschlossen!")
    }
}
