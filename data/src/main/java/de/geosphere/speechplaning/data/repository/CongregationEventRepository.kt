package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.base.BaseFirestoreSubcollectionRepository
import de.geosphere.speechplaning.data.repository.services.FirestoreService

private const val CONGREGATION_EVENTS_SUBCOLLECTION = "congregationEvents"
// Konstanten für übergeordnete Collections, wie in anderen Repositories definiert
private const val CONGREGATIONS_SUBCOLLECTION = "congregations"
private const val DISTRICTS_COLLECTION = "districts"

class CongregationEventRepository(
    firestoreService: FirestoreService
) : BaseFirestoreSubcollectionRepository<CongregationEvent>(
    firestoreService = firestoreService,
    subcollectionName = CONGREGATION_EVENTS_SUBCOLLECTION,
    clazz = CongregationEvent::class.java
) {

    override fun extractIdFromEntity(entity: CongregationEvent): String {
        return entity.id
    }

    /**
     * Erstellt den Pfad zur Collection, die das Elterndokument (Congregation) enthält.
     * Benötigt districtId als parentIds[0] und congregationId als parentIds[1].
     * Der Pfad ist: districts/{districtId}/congregations
     */
    override fun buildParentCollectionPath(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        val districtId = parentIds[0]
        // Der Pfad zur Collection, die das Elterndokument (Congregation) enthält.
        return "${DISTRICTS_COLLECTION}/${districtId}/${CONGREGATIONS_SUBCOLLECTION}"
    }

    /**
     * Ermittelt die ID des direkten Elterndokuments (Congregation) der Subcollection.
     * Benötigt districtId als parentIds[0] und congregationId als parentIds[1].
     * Die ID des direkten Parent-Dokuments ist die congregationId.
     */
    override fun getParentDocumentId(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        // Die ID des direkten Elterndokuments (Congregation).
        return parentIds[1] // congregationId
    }

    /**
     * Speichert ein CongregationEvent in der Subcollection einer bestimmten Congregation.
     *
     * @param districtId Die ID des Districts, zu dem die Congregation gehört.
     * @param congregationId Die ID der Congregation, zu der das Event gehört.
     * @param event Das zu speichernde CongregationEvent-Objekt.
     * @return Die ID des gespeicherten Dokuments.
     */
    suspend fun saveEvent(districtId: String, congregationId: String, event: CongregationEvent): String {
        return super.save(event, districtId, congregationId)
    }

    /**
     * Ruft ein spezifisches CongregationEvent anhand seiner ID ab.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der Congregation.
     * @param eventId Die ID des abzurufenden Events.
     * @return Das gefundene CongregationEvent oder null.
     */
    suspend fun getEventById(districtId: String, congregationId: String, eventId: String): CongregationEvent? {
        return super.getById(eventId, districtId, congregationId)
    }

    /**
     * Ruft alle CongregationEvents für eine bestimmte Congregation ab.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der Congregation.
     * @return Eine Liste von CongregationEvent-Objekten.
     */
    suspend fun getAllEventsForCongregation(districtId: String, congregationId: String): List<CongregationEvent> {
        return super.getAll(districtId, congregationId)
    }

    /**
     * Löscht ein bestimmtes CongregationEvent.
     *
     * @param districtId Die ID des Districts.
     * @param congregationId Die ID der Congregation.
     * @param eventId Die ID des zu löschenden Events.
     */
    suspend fun deleteEvent(districtId: String, congregationId: String, eventId: String) {
        super.delete(eventId, districtId, congregationId)
    }

    // Hinweis: Methoden, die komplexere Queries direkt über den FirestoreService benötigen
    // (z.B. Filtern nach Datum oder Rückgabe von Flow), müssten hier spezifisch implementiert werden,
    // da die Basisklasse dies nicht generisch abdeckt.
}
