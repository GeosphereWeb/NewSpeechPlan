package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreSubcollectionRepository<T : SavableDataClass, PID : Any>(
    private val subcollectionActions: ISubcollectionActions,
    private val subcollectionName: String,
    private val clazz: Class<T>
) : IFirestoreSubcollectionRepository<T, PID> {

    abstract fun extractIdFromEntity(entity: T): String

    // Methoden, die von den abgeleiteten Klassen implementiert werden müssen
    abstract fun buildParentCollectionPath(vararg parentIds: PID): String
    abstract fun getParentDocumentId(vararg parentIds: PID): String

    override suspend fun save(entity: T, vararg parentIds: PID): String {
        // Unsafe casts, assuming PID is always String for Firestore path construction.
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        val entityId = extractIdFromEntity(entity)

        return try {
            if (entityId.isBlank()) {
                subcollectionActions.addDocumentToSubcollection(
                    parentCollectionPath,
                    parentDocId,
                    subcollectionName,
                    entity
                )
            } else {
                subcollectionActions.setDocumentInSubcollection(
                    parentCollectionPath,
                    parentDocId,
                    subcollectionName,
                    entityId,
                    entity
                )
                entityId
            }
        } catch (e: Exception) {
            val idForErrorMessage = entityId.ifBlank { "[new]" }
            throw RuntimeException(
                "Failed to save entity '$idForErrorMessage' in subcollection '$subcollectionName' " +
                    "under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    @Suppress("ReturnCount")
    override suspend fun getById(id: String, vararg parentIds: PID): T? {
        if (id.isBlank()) return null
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        return try {
            subcollectionActions.getDocumentFromSubcollection(
                parentCollectionPath,
                parentDocId,
                subcollectionName,
                id,
                clazz
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$id' from subcollection '$subcollectionName' under parent " +
                    "'$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override suspend fun delete(id: String, vararg parentIds: PID) {
        require(id.isNotBlank()) { "Document ID cannot be blank for deletion." }
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        try {
            subcollectionActions.deleteDocumentFromSubcollection(
                parentCollectionPath,
                parentDocId,
                subcollectionName,
                id
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$id' from subcollection '$subcollectionName' " +
                    "under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override fun getAllFlow(vararg parentIds: PID): Flow<List<T>> {
        throw UnsupportedOperationException("Subcollection flow is not implemented yet.")
    }
}
