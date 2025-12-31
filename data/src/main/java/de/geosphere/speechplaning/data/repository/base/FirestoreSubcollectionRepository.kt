package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreSubcollectionRepository<T : SavableDataClass, ID : Any, PID : Any>(
    private val subcollectionActions: ISubcollectionActions,
    private val flowActions: IFlowActions,
    private val subcollectionName: String,
    private val clazz: Class<T>
) : IFirestoreSubcollectionRepository<T, ID, PID> {

    abstract fun extractIdFromEntity(entity: T): ID

    // Methoden, die von den abgeleiteten Klassen implementiert werden müssen
    abstract fun buildParentCollectionPath(vararg parentIds: PID): String
    abstract fun getParentDocumentId(vararg parentIds: PID): String

    override suspend fun save(entity: T, vararg parentIds: PID): ID {
        // Unsafe casts, assuming PID is always String for Firestore path construction.
        val stringParentIds = parentIds.map { it as String }.toTypedArray()
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        val entityId = extractIdFromEntity(entity)

        return try {
            val documentId = entityId as? String ?: ""
            if (documentId.isBlank()) {
                subcollectionActions.addDocumentToSubcollection(
                    parentCollectionPath,
                    parentDocId,
                    subcollectionName,
                    entity
                ) as ID
            } else {
                subcollectionActions.setDocumentInSubcollection(
                    parentCollectionPath,
                    parentDocId,
                    subcollectionName,
                    documentId,
                    entity
                )
                entityId
            }
        } catch (e: Exception) {
            val idForErrorMessage = if ((entityId as? String)?.isBlank() != false) "[new]" else entityId.toString()
            throw RuntimeException(
                "Failed to save entity '$idForErrorMessage' in subcollection '$subcollectionName' " +
                    "under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override suspend fun getById(id: ID, vararg parentIds: PID): T? {
        val documentId = id as? String ?: return null
        if (documentId.isBlank()) return null
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        return try {
            subcollectionActions.getDocumentFromSubcollection(
                parentCollectionPath,
                parentDocId,
                subcollectionName,
                documentId,
                clazz
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$documentId' from subcollection '$subcollectionName' under parent " +
                    "'$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override suspend fun delete(id: ID, vararg parentIds: PID) {
        val documentId = id as? String ?: throw IllegalArgumentException("ID must be a non-blank String for deletion.")
        require(documentId.isNotBlank()) { "Document ID cannot be blank for deletion." }
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        try {
            subcollectionActions.deleteDocumentFromSubcollection(
                parentCollectionPath,
                parentDocId,
                subcollectionName,
                documentId
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$documentId' from subcollection '$subcollectionName' " +
                    "under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override fun getAllFlow(vararg parentIds: PID): Flow<List<T>> {
        throw UnsupportedOperationException("Subcollection flow is not implemented yet.")
    }
}
