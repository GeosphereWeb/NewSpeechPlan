package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.base.BaseFirestoreSubcollectionRepository
import de.geosphere.speechplaning.data.repository.services.FirestoreService

private const val SPEAKERS_SUBCOLLECTION = "speakers"
private const val DISTRICTS_COLLECTION = "districts"
private const val CONGREGATIONS_SUBCOLLECTION = "congregations"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class SpeakerRepository(
    firestoreService: FirestoreService
) : BaseFirestoreSubcollectionRepository<Speaker>(
    firestoreService = firestoreService,
    subcollectionName = SPEAKERS_SUBCOLLECTION,
    clazz = Speaker::class.java
) {

    override fun extractIdFromEntity(entity: Speaker): String {
        return entity.id
    }

    /**
     * Erwartet districtId als parentIds[0] und congregationId als parentIds[1].
     */
    override fun buildParentCollectionPath(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        val districtId = parentIds[0]
        // Der Pfad zur Collection, die das Elterndokument (Congregation) enthält.
        return "$DISTRICTS_COLLECTION/$districtId/$CONGREGATIONS_SUBCOLLECTION"
    }

    /**
     * Erwartet districtId als parentIds[0] und congregationId als parentIds[1].
     * Gibt die congregationId zurück, da dies das direkte Elterndokument der Speakers-Subcollection ist.
     */
    override fun getParentDocumentId(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        return parentIds[1] // congregationId
    }

    /**
     * Speichert einen Redner in der Subcollection einer bestimmten Versammlung.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der Versammlung.
     * @param speaker Das zu speichernde Speaker-Objekt.
     * @return Die ID des gespeicherten Dokuments.
     */
    suspend fun saveSpeaker(districtId: String, congregationId: String, speaker: Speaker): String {
        // Ruft die save-Methode der Basisklasse auf und übergibt die parentIds in der korrekten Reihenfolge.
        return super.save(speaker, districtId, congregationId)
    }

    /**
     * Ruft alle Redner für eine bestimmte Versammlung ab.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der Versammlung.
     * @return Eine Liste von Speaker-Objekten.
     */
    suspend fun getSpeakersForCongregation(districtId: String, congregationId: String): List<Speaker> {
        // Ruft die getAll-Methode der Basisklasse auf.
        return super.getAll(districtId, congregationId)
    }

    /**
     * Löscht einen bestimmten Redner.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der Versammlung, zu der der Redner gehört.
     * @param speakerId Die ID des zu löschenden Redners.
     */
    suspend fun deleteSpeaker(districtId: String, congregationId: String, speakerId: String) {
        // Ruft die delete-Methode der Basisklasse auf.
        super.delete(speakerId, districtId, congregationId)
    }
}
