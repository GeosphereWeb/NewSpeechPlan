package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.data.repository.services.ICollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreRepository<T : SavableDataClass>(
    private val collectionActions: ICollectionActions,
    private val flowActions: IFlowActions,
    private val collectionPath: String,
    private val clazz: Class<T>
) : IFirestoreRepository<T, String> {

    // Abstrakte Methode zur Extraktion der ID aus einer Entität
    abstract fun extractIdFromEntity(entity: T): String

    override suspend fun save(entity: T): String {
        val id = extractIdFromEntity(entity)
        return try {
            // Firestore IDs are strings, so we need to handle this. We assume ID is String for now.
            val documentId = id
            if (documentId.isBlank()) {
                collectionActions.addDocument(collectionPath, entity)
            } else {
                collectionActions.setDocument(collectionPath, documentId, entity)
                id
            }
        } catch (e: Exception) {
            val idForErrorMessage = id.ifBlank { "[new]" }
            throw RuntimeException("Failed to save entity '$idForErrorMessage' in collection '$collectionPath'", e)
        }
    }

    @Suppress("ReturnCount")
    override suspend fun getById(id: String): T? {
        if (id.isBlank()) return null
        return try {
            collectionActions.getDocument(collectionPath, id, clazz)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get entity '$id' from collection '$collectionPath'", e)
        }
    }

    override suspend fun delete(id: String) {
        require(id.isNotBlank()) { "Document ID cannot be blank for deletion." }
        try {
            collectionActions.deleteDocument(collectionPath, id)
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete entity '$id' from collection '$collectionPath'", e)
        }
    }

    override fun getAllFlow(): Flow<List<T>> {
        return flowActions.getCollectionFlow(collectionPath, clazz)
    }
}
