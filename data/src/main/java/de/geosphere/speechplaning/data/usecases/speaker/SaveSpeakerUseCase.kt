package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepositoryImpl

@Suppress("ReturnCount")
class SaveSpeakerUseCase(private val repository: SpeakerRepositoryImpl) {

    /**
     * Variante 1: Speichert einen Speaker basierend auf den IDs im Objekt.
     * Nur für Updates am SELBEN Ort geeignet.
     */
    suspend operator fun invoke(speaker: Speaker): Result<Unit> {
        if (speaker.nameLast.isBlank()) {
            return Result.failure(IllegalArgumentException("Speaker name cannot be blank."))
        }
        if (speaker.districtId.isBlank() || speaker.congregationId.isBlank()) {
            return Result.failure(
                IllegalArgumentException(
                    "DistrictId and CongregationId must not be " +
                        "blank when saving a speaker without context."
                )
            )
        }

        return try {
            repository.saveSpeaker(
                districtId = speaker.districtId,
                congregationId = speaker.congregationId,
                speaker = speaker
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Variante 2: Speichert einen Speaker im Kontext einer spezifischen Congregation.
     * Ideal für "Create New".
     */
    suspend operator fun invoke(speaker: Speaker, congregation: Congregation): Result<Unit> {
        val updatedSpeaker = speaker.copy(
            districtId = congregation.district,
            congregationId = congregation.id
        )
        return this.invoke(updatedSpeaker)
    }

    /**
     * Variante 3 (MOVE): Speichert Änderungen und behandelt Umzüge (Congregation-Wechsel).
     *
     * WICHTIG ZUR ID:
     * Die UID (speaker.id) wird hierbei BEIBEHALTEN.
     * Da das [speaker]-Objekt eine ID hat, führt das Repository ein 'set()' am neuen Pfad aus,
     * anstatt eine neue ID zu generieren. Referenzen, die nur auf der ID basieren, bleiben also gültig.
     * Referenzen, die auf dem absoluten Pfad basieren (DocumentReference), werden ungültig.
     */
    suspend operator fun invoke(
        speaker: Speaker,
        oldDistrictId: String,
        oldCongregationId: String
    ): Result<Unit> {
        // Prüfen, ob IDs valide sind (für das neue Ziel)
        if (speaker.districtId.isBlank() || speaker.congregationId.isBlank()) {
            return Result.failure(IllegalArgumentException("New DistrictId/CongregationId cannot be blank."))
        }

        // Sicherheitscheck: Ein Move geht nur mit existierender ID!
        if (speaker.id.isBlank()) {
            return Result.failure(IllegalArgumentException("Cannot move a speaker without an existing ID."))
        }

        // Check: Hat sich der Ort wirklich geändert?
        val hasMoved = speaker.districtId != oldDistrictId || speaker.congregationId != oldCongregationId

        return try {
            // 1. Speichern am (neuen) Ort
            // Das Repository nutzt speaker.id als Dokumenten-Namen -> ID bleibt gleich!
            repository.saveSpeaker(
                districtId = speaker.districtId,
                congregationId = speaker.congregationId,
                speaker = speaker
            )

            // 2. Wenn es ein Umzug war -> Altes Dokument löschen
            // Wir machen das erst NACH dem erfolgreichen Speichern, um Datenverlust zu vermeiden.
            if (hasMoved && oldDistrictId.isNotBlank() && oldCongregationId.isNotBlank()) {
                repository.deleteSpeaker(
                    districtId = oldDistrictId,
                    congregationId = oldCongregationId,
                    speakerId = speaker.id
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
