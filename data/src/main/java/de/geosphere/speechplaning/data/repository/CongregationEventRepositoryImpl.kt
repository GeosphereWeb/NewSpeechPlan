package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.base.FirestoreSubcollectionRepository
import de.geosphere.speechplaning.data.repository.services.IFirestoreService
import kotlinx.coroutines.flow.Flow

private const val CONGREGATION_EVENTS_COLLECTION = "congregationEvents"
private const val DISTRICTS_COLLECTION = "districts"
private const val CONGREGATIONS_SUBCOLLECTION = "congregations"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown", "TooManyFunctions")
class CongregationEventRepositoryImpl(
    firestoreService: IFirestoreService
) : FirestoreSubcollectionRepository<CongregationEvent>(
    firestoreService = firestoreService,
    subcollectionName = CONGREGATION_EVENTS_COLLECTION,
    clazz = CongregationEvent::class.java
) {

    // Implementierung der abstrakten Methode aus FirestoreSubcollectionRepository
    override fun extractIdFromEntity(entity: CongregationEvent): String {
        return entity.id
    }

    /**
     * Erwartet districtId als parentIds[0] und congregationId als parentIds[1].
     */
    override fun buildParentCollectionPath(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        val districtId = parentIds[0]
        return "$DISTRICTS_COLLECTION/$districtId/$CONGREGATIONS_SUBCOLLECTION"
    }

    /**
     * Erwartet districtId als parentIds[0] und congregationId als parentIds[1].
     */
    override fun getParentDocumentId(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        return parentIds[1]
    }

    // ------------------------- Convenience wrapper methods -------------------------
    suspend fun saveEvent(districtId: String, congregationId: String, event: CongregationEvent): String {
        return super.save(event, districtId, congregationId)
    }

    suspend fun getEventById(districtId: String, congregationId: String, eventId: String): CongregationEvent? {
        return super.getById(eventId, districtId, congregationId)
    }

    suspend fun getAllEventsForCongregation(districtId: String, congregationId: String): List<CongregationEvent> {
        return super.getAll(districtId, congregationId)
    }

    suspend fun deleteEvent(districtId: String, congregationId: String, eventId: String) {
        super.delete(eventId, districtId, congregationId)
    }

    // ------------------------- Collection-group / top-level helpers -------------------------
    suspend fun getAllEvents(): List<CongregationEvent> {
        // Delegiert an den Service (CollectionGroup)
        return firestoreService.getDocumentsFromSubcollection(
            parentCollection = "",
            parentId = "",
            subcollection = CONGREGATION_EVENTS_COLLECTION,
            objectClass = CongregationEvent::class.java
        )
    }

    fun getAllEventsFlow(): Flow<List<CongregationEvent>> {
        return firestoreService.getCollectionGroupFlow(CONGREGATION_EVENTS_COLLECTION, CongregationEvent::class.java)
    }

    suspend fun saveEvent(event: CongregationEvent): String {
        // Top-level save: wenn ID vorhanden -> set(documentId), sonst add()
        return if (event.id.isBlank()) {
            firestoreService.saveDocument(CONGREGATION_EVENTS_COLLECTION, event)
        } else {
            val success = firestoreService.saveDocumentWithId(CONGREGATION_EVENTS_COLLECTION, event.id, event)
            if (success) event.id else throw RuntimeException("Failed to save document with id ${event.id}")
        }
    }

    suspend fun deleteEvent(eventId: String) {
        super.delete(eventId)
    }
}
