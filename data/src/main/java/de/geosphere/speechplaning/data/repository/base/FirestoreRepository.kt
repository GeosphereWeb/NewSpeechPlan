package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.data.repository.services.ICollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import kotlinx.coroutines.flow.Flow

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreRepository<T : SavableDataClass, ID : Any>(
    private val collectionActions: ICollectionActions,
    private val flowActions: IFlowActions,
    private val collectionPath: String,
    private val clazz: Class<T>
) : IFirestoreRepository<T, ID> {

    // Abstrakte Methode zur Extraktion der ID aus einer Entität
    abstract fun extractIdFromEntity(entity: T): ID

    override suspend fun save(entity: T): ID {
        val id = extractIdFromEntity(entity)
        return try {
            // Firestore IDs are strings, so we need to handle this. We assume ID is String for now.
            val documentId = id as? String ?: ""
            if (documentId.isBlank()) {
                collectionActions.addDocument(collectionPath, entity) as ID
            } else {
                collectionActions.setDocument(collectionPath, documentId, entity)
                id
            }
        } catch (e: Exception) {
            val idForErrorMessage = if ((id as? String)?.isBlank() != false) "[new]" else id.toString()
            throw RuntimeException("Failed to save entity '$idForErrorMessage' in collection '$collectionPath'", e)
        }
    }

    override suspend fun getById(id: ID): T? {
        val documentId = id as? String ?: return null
        if (documentId.isBlank()) return null
        return try {
            collectionActions.getDocument(collectionPath, documentId, clazz)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get entity '$documentId' from collection '$collectionPath'", e)
        }
    }

    override suspend fun delete(id: ID) {
        val documentId = id as? String ?: throw IllegalArgumentException("ID must be a non-blank String for deletion.")
        require(documentId.isNotBlank()) { "Document ID cannot be blank for deletion." }
        try {
            collectionActions.deleteDocument(collectionPath, documentId)
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete entity '$documentId' from collection '$collectionPath'", e)
        }
    }

    override fun getAllFlow(): Flow<List<T>> {
        return flowActions.getCollectionFlow(collectionPath, clazz)
    }
}
