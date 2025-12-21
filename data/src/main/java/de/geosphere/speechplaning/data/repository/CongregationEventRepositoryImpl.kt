package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.services.IFirestoreService
import kotlinx.coroutines.flow.Flow

private const val CONGREGATION_EVENTS_COLLECTION = "congregationEvents"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class CongregationEventRepositoryImpl(
    private val firestoreService: IFirestoreService
) {

    // Implementierung der abstrakten Methode(n) für Tests
    internal fun extractIdFromEntity(entity: CongregationEvent): String {
        return entity.id
    }

    // --------------------------------------------------
    // Subcollection-spezifische Methoden (für Districts/Congregations/...)
    // --------------------------------------------------

    suspend fun saveEvent(districtId: String, congregationId: String, event: CongregationEvent): String {
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocumentId = getParentDocumentId(districtId, congregationId)

        return try {
            if (event.id.isBlank()) {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = parentCollectionPath,
                    parentId = parentDocumentId,
                    subcollection = CONGREGATION_EVENTS_COLLECTION,
                    data = event
                )
            } else {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = parentCollectionPath,
                    parentId = parentDocumentId,
                    subcollection = CONGREGATION_EVENTS_COLLECTION,
                    documentId = event.id,
                    data = event
                )
                event.id
            }
        } catch (e: Exception) {
            val idForMessage = if (event.id.isBlank()) "[new]" else event.id
            throw RuntimeException(
                "Failed to save entity '$idForMessage' in subcollection '$CONGREGATION_EVENTS_COLLECTION' " +
                    "under parent '$parentDocumentId' in '$parentCollectionPath'",
                e
            )
        }
    }

    suspend fun getEventById(districtId: String, congregationId: String, eventId: String): CongregationEvent? {
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocumentId = getParentDocumentId(districtId, congregationId)

        return try {
            if (eventId.isBlank()) return null
            firestoreService.getDocumentFromSubcollection(
                parentCollectionPath = parentCollectionPath,
                parentDocumentId = parentDocumentId,
                subcollectionName = CONGREGATION_EVENTS_COLLECTION,
                documentId = eventId,
                objectClass = CongregationEvent::class.java
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$eventId' from subcollection '$CONGREGATION_EVENTS_COLLECTION' " +
                    "under parent '$parentDocumentId' in '$parentCollectionPath'",
                e
            )
        }
    }

    suspend fun getAllEventsForCongregation(districtId: String, congregationId: String): List<CongregationEvent> {
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocumentId = getParentDocumentId(districtId, congregationId)

        return try {
            firestoreService.getDocumentsFromSubcollection(
                parentCollection = parentCollectionPath,
                parentId = parentDocumentId,
                subcollection = CONGREGATION_EVENTS_COLLECTION,
                objectClass = CongregationEvent::class.java
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get all entities from subcollection '$CONGREGATION_EVENTS_COLLECTION' " +
                    "under parent '$parentDocumentId' in '$parentCollectionPath'",
                e
            )
        }
    }

    suspend fun deleteEvent(districtId: String, congregationId: String, eventId: String) {
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocumentId = getParentDocumentId(districtId, congregationId)

        try {
            require(eventId.isNotBlank()) { "Document ID cannot be blank for deletion." }
            firestoreService.deleteDocumentFromSubcollection(
                parentCollection = parentCollectionPath,
                parentId = parentDocumentId,
                subcollection = CONGREGATION_EVENTS_COLLECTION,
                documentId = eventId
            )
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$eventId' from subcollection '$CONGREGATION_EVENTS_COLLECTION' " +
                    "under parent '$parentDocumentId' in '$parentCollectionPath'",
                e
            )
        }
    }

    // --------------------------------------------------
    // Allgemeine (Collection-level) Methoden
    // --------------------------------------------------

    suspend fun getAllEvents(): List<CongregationEvent> {
        return firestoreService.getDocumentsFromSubcollection(
            parentCollection = "", // nicht verwendet hier, aber Tests greifen normalerweise auf subcollection-Methoden
            parentId = "",
            subcollection = CONGREGATION_EVENTS_COLLECTION,
            objectClass = CongregationEvent::class.java
        )
    }

    fun getAllEventsFlow(): Flow<List<CongregationEvent>> {
        return firestoreService.getCollectionGroupFlow(CONGREGATION_EVENTS_COLLECTION, CongregationEvent::class.java)
    }

    suspend fun saveEvent(event: CongregationEvent): String {
        // delegiert auf SaveDocument wenn top-level gewünscht
        return firestoreService.saveDocument(CONGREGATION_EVENTS_COLLECTION, event)
    }

    suspend fun deleteEvent(eventId: String) {
        // Top-level Löschung eines CongregationEvent-Dokuments
        try {
            require(eventId.isNotBlank()) { "Document ID cannot be blank for deletion." }
            firestoreService.deleteDocument(CONGREGATION_EVENTS_COLLECTION, eventId)
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete top-level entity '$eventId' from collection '$CONGREGATION_EVENTS_COLLECTION'",
                e
            )
        }
    }

    // --------------------------------------------------
    // Hilfsfunktionen
    // --------------------------------------------------

    internal fun buildParentCollectionPath(vararg parentIds: String): String {
        // Erwartet (districtId, congregationId) und liefert "districts/{districtId}/congregations"
        if (parentIds.size != 2) throw IllegalArgumentException("Expected districtId and congregationId as parentIds")
        val districtCollection = "districts"
        val congregationsSubcollection = "congregations"
        return "$districtCollection/${parentIds[0]}/$congregationsSubcollection"
    }

    internal fun getParentDocumentId(vararg parentIds: String): String {
        if (parentIds.size != 2) throw IllegalArgumentException("Expected districtId and congregationId as parentIds")
        return parentIds[1]
    }
}
