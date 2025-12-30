package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.repository.base.FirestoreRepository
import de.geosphere.speechplaning.data.repository.services.IFirestoreService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val CONGREGATION_EVENTS_COLLECTION = "congregationEvents"
private const val DISTRICTS_COLLECTION = "districts"
private const val CONGREGATIONS_SUBCOLLECTION = "congregations"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown", "TooManyFunctions")
class CongregationEventRepositoryImpl(
    firestoreService: IFirestoreService
) : FirestoreRepository<CongregationEvent>(
    firestoreService = firestoreService,
    collectionPath = CONGREGATION_EVENTS_COLLECTION,
    clazz = CongregationEvent::class.java
) {

    // Implementierung der abstrakten Methode aus FirestoreRepository
    override fun extractIdFromEntity(entity: CongregationEvent): String {
        return entity.id
    }

    // ------------------------- Top-level Convenience wrapper methods -------------------------
    suspend fun saveEvent(event: CongregationEvent): String {
        return save(event)
    }

    suspend fun getEventById(eventId: String): CongregationEvent? {
        return getById(eventId)
    }

    suspend fun getAllEvents(): List<CongregationEvent> {
        return getAll()
    }

    suspend fun deleteEvent(eventId: String) {
        delete(eventId)
    }

    fun getAllEventsFlow(): Flow<List<CongregationEvent>> {
        return firestoreService.getCollectionGroupFlow(CONGREGATION_EVENTS_COLLECTION, CongregationEvent::class.java)
    }

    // Add subcollection flow compatibility
    fun getAllFlow(vararg parentIds: String): Flow<List<CongregationEvent>> = callbackFlow {
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)

        val collectionRef = firestoreService.getSubcollection(
            parentCollection = actualParentCollectionPath,
            parentId = actualParentDocumentId,
            subcollection = CONGREGATION_EVENTS_COLLECTION
        )

        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val data = snapshot.toObjects(CongregationEvent::class.java)
                trySend(data)
            }
        }

        awaitClose { listenerRegistration.remove() }
    }

    // ------------------------- Subcollection-compatible APIs (Kompatibilität) -------------------------

    /**
     * Erwartet districtId als parentIds[0] und congregationId als parentIds[1].
     */
    fun buildParentCollectionPath(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        val districtId = parentIds[0]
        return "$DISTRICTS_COLLECTION/$districtId/$CONGREGATIONS_SUBCOLLECTION"
    }

    /**
     * Erwartet districtId als parentIds[0] und congregationId als parentIds[1].
     */
    fun getParentDocumentId(vararg parentIds: String): String {
        require(parentIds.size == 2) { "Expected districtId and congregationId as parentIds" }
        return parentIds[1]
    }

    /**
     * Kompatible save-Methode im Subcollection-Stil (wie früher).
     */
    suspend fun saveEvent(districtId: String, congregationId: String, event: CongregationEvent): String {
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocId = getParentDocumentId(districtId, congregationId)
        val entityId = extractIdFromEntity(event)

        return try {
            if (entityId.isBlank()) {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = parentCollectionPath,
                    parentId = parentDocId,
                    subcollection = CONGREGATION_EVENTS_COLLECTION,
                    data = event
                )
            } else {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = parentCollectionPath,
                    parentId = parentDocId,
                    subcollection = CONGREGATION_EVENTS_COLLECTION,
                    documentId = entityId,
                    data = event
                )
                entityId
            }
        } catch (e: Exception) {
            val idForErrorMessage = if (entityId.isBlank()) "[new]" else entityId
            throw RuntimeException(
                "Failed to save entity '$idForErrorMessage' in subcollection '" +
                    "${CONGREGATION_EVENTS_COLLECTION}' under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    suspend fun getEventById(districtId: String, congregationId: String, eventId: String): CongregationEvent? {
        if (eventId.isBlank()) return null
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocId = getParentDocumentId(districtId, congregationId)
        return try {
            firestoreService.getDocumentFromSubcollection(
                parentCollectionPath = parentCollectionPath,
                parentDocumentId = parentDocId,
                subcollectionName = CONGREGATION_EVENTS_COLLECTION,
                documentId = eventId,
                objectClass = CongregationEvent::class.java
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$eventId' from subcollection '${CONGREGATION_EVENTS_COLLECTION}' under " +
                    "parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    suspend fun getAllEventsForCongregation(districtId: String, congregationId: String): List<CongregationEvent> {
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocId = getParentDocumentId(districtId, congregationId)
        return try {
            firestoreService.getDocumentsFromSubcollection(
                parentCollection = parentCollectionPath,
                parentId = parentDocId,
                subcollection = CONGREGATION_EVENTS_COLLECTION,
                objectClass = CongregationEvent::class.java
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get all entities from subcollection '${CONGREGATION_EVENTS_COLLECTION}' under " +
                    "parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    suspend fun deleteEvent(districtId: String, congregationId: String, eventId: String) {
        require(eventId.isNotBlank()) { "Document ID cannot be blank for deletion." }
        val parentCollectionPath = buildParentCollectionPath(districtId, congregationId)
        val parentDocId = getParentDocumentId(districtId, congregationId)
        try {
            firestoreService.deleteDocumentFromSubcollection(
                parentCollection = parentCollectionPath,
                parentId = parentDocId,
                subcollection = CONGREGATION_EVENTS_COLLECTION,
                documentId = eventId
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$eventId' from subcollection '${CONGREGATION_EVENTS_COLLECTION}' under " +
                    "parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }
}
