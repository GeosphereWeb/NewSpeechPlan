package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreSubcollectionRepository<T : SavableDataClass>(
    private val subcollectionActions: ISubcollectionActions,
    private val flowActions: IFlowActions,
    private val subcollectionName: String,
    private val clazz: Class<T>
) : IFirestoreSubcollectionRepository<T> {

    abstract fun extractIdFromEntity(entity: T): String

    // Methoden, die von den abgeleiteten Klassen implementiert werden müssen
    abstract fun buildParentCollectionPath(vararg parentIds: String): String
    abstract fun getParentDocumentId(vararg parentIds: String): String

    override suspend fun save(entity: T, vararg parentIds: String): String {
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        val entityId = extractIdFromEntity(entity)

        return try {
            if (entityId.isBlank()) {
                subcollectionActions.addDocumentToSubcollection(parentCollectionPath, parentDocId, subcollectionName, entity)
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
            val idForErrorMessage = if (entityId.isBlank()) "[new]" else entityId
            throw RuntimeException(
                "Failed to save entity '$idForErrorMessage' in subcollection '$subcollectionName' under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override suspend fun getById(id: String, vararg parentIds: String): T? {
        if (id.isBlank()) return null
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        return try {
            subcollectionActions.getDocumentFromSubcollection(parentCollectionPath, parentDocId, subcollectionName, id, clazz)
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$id' from subcollection '$subcollectionName' under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override suspend fun getAll(vararg parentIds: String): List<T> {
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        return try {
            subcollectionActions.getDocumentsFromSubcollection(parentCollectionPath, parentDocId, subcollectionName, clazz)
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get all entities from subcollection '$subcollectionName' under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override suspend fun delete(id: String, vararg parentIds: String) {
        require(id.isNotBlank()) { "Document ID cannot be blank for deletion." }
        val parentCollectionPath = buildParentCollectionPath(*parentIds)
        val parentDocId = getParentDocumentId(*parentIds)
        try {
            subcollectionActions.deleteDocumentFromSubcollection(parentCollectionPath, parentDocId, subcollectionName, id)
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$id' from subcollection '$subcollectionName' under parent '$parentDocId' in '$parentCollectionPath'",
                e
            )
        }
    }

    override fun getAllFlow(vararg parentIds: String): Flow<List<T>> {
        // This implementation needs a specific method in FlowActions that can handle subcollection flows.
        // The current getCollectionFlow is not sufficient as it does not know about parents.
        // Let's create a placeholder or throw an exception.
        throw UnsupportedOperationException("Subcollection flow is not implemented yet.")
    }
}
