package de.geosphere.speechplaning.helper

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.io.File

suspend fun main() {
    val importer = CsvImporter()
    importer.importData(
        csvFilePath = "C:/Users/werne/StudioProjects/NewSpeechPlan/Helper/Mappe1.CSV"
    )
}

class CsvImporter {

    // Initialisiere Firestore
    private val db = Firebase.firestore

    suspend fun importData(csvFilePath: String) {
        val lines = File(csvFilePath).readLines().drop(1) // Header überspringen

        val districts = mutableMapOf<String, District>()
        val congregations = mutableMapOf<String, Congregation>()
        val speakers = mutableMapOf<String, Speaker>()
        val speeches = mutableMapOf<String, Speech>()
        val speakerSpeechMap = mutableMapOf<String, MutableList<String>>()

        lines.forEach { line ->
            val columns = line.split(';')

            // --- Kreis (District) verarbeiten ---
            val districtId = columns[0].trim()
            if (districtId.isNotBlank() && !districts.containsKey(districtId)) {
                districts[districtId] = District(
                    id = districtId,
                    name = columns[1].trim()
                )
            }

            // --- Versammlung (Congregation) verarbeiten ---
            val congregationId = columns[2].trim()
            if (congregationId.isNotBlank() && !congregations.containsKey(congregationId)) {
                val address = "${columns[4].trim()} ${columns[5].trim()} ${columns[6].trim()}, ${columns[7].trim()} ${columns[8].trim()}".trim()
                congregations[congregationId] = Congregation(
                    id = congregationId,
                    name = columns[3].trim(),
                    district = districtId,
                    address = address.replace(Regex(" ,$"), "").trim(),
                    active = columns[11].trim().equals("WAHR", ignoreCase = true)
                    // meetingTime ist in der CSV nicht eindeutig vorhanden
                )
            }

            // --- Redner (Speaker) verarbeiten ---
            val speakerId = columns[12].trim()
            if (speakerId.isNotBlank() && !speakers.containsKey(speakerId)) {
                speakers[speakerId] = Speaker(
                    id = speakerId,
                    lastName = columns[13].trim(),
                    firstName = columns[14].trim(),
                    phone = columns[15].trim(),
                    phone2 = columns[16].trim(),
                    email = columns[17].trim(),
                    congregationId = columns[18].trim(),
                    active = columns[20].trim().equals("WAHR", ignoreCase = true),
                    note = columns[22].trim()
                    // speeches Liste wird später gefüllt
                )
            }

            // --- Vortrag (Speech) verarbeiten ---
            val speechNumber = columns[23].trim()
            if (speechNumber.isNotBlank() && !speeches.containsKey(speechNumber)) {
                speeches[speechNumber] = Speech(
                    id = speechNumber, // Wir verwenden die Nummer als ID
                    number = speechNumber,
                    subject = columns[24].trim(),
                    active = columns[25].trim().equals("WAHR", ignoreCase = true)
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
            speakers[speakerId]?.let {
                speakers[speakerId] = it.copy(speeches = speechIds)
            }
        }

        // --- Daten in Firestore schreiben ---
        println("Schreibe ${districts.size} Kreise...")
        districts.values.forEach { db.collection("districts").document(it.id).set(it).await() }

        println("Schreibe ${congregations.size} Versammlungen...")
        congregations.values.forEach { db.collection("congregations").document(it.id).set(it).await() }

        // println("Schreibe ${speeches.size} Vorträge...")
        // speeches.values.forEach { db.collection("speeches").document(it.id).set(it).await() }

        println("Schreibe ${speakers.size} Redner...")
        speakers.values.forEach { db.collection("speakers").document(it.id).set(it).await() }

        println("Import abgeschlossen!")
    }
}
