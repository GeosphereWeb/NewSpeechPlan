package de.geosphere.speechplaning.data.repository.services

import com.google.firebase.firestore.CollectionReference
import kotlinx.coroutines.flow.Flow

/**
 * Interface for actions on top-level collections.
 */
interface ICollectionActions {
    suspend fun <T : Any> addDocument(collectionPath: String, data: T): String
    suspend fun <T : Any> setDocument(collectionPath: String, documentId: String, data: T)
    suspend fun <T : Any> getDocument(collectionPath: String, documentId: String, objectClass: Class<T>): T?
    suspend fun <T : Any> getDocuments(collectionPath: String, objectClass: Class<T>): List<T>
    suspend fun deleteDocument(collectionPath: String, documentId: String)

    // Project-specific helpers for top-level collections
    suspend fun <T : Any> saveDocument(collectionPath: String, data: T): String
    suspend fun <T : Any> saveDocumentWithId(collectionPath: String, documentId: String, data: T): Boolean
}

/**
 * Interface for actions on subcollections.
 */
interface ISubcollectionActions {
    suspend fun <T : Any> addDocumentToSubcollection(
        parentCollection: String, parentId: String, subcollection: String,
        data: T
    ): String

    suspend fun <T : Any> setDocumentInSubcollection(
        parentCollection: String, parentId: String, subcollection: String,
        documentId: String, data: T
    )

    suspend fun <T : Any> getDocumentFromSubcollection(
        parentCollectionPath: String, parentDocumentId: String,
        subcollectionName: String, documentId: String, objectClass: Class<T>
    ): T?

    suspend fun <T : Any> getDocumentsFromSubcollection(
        parentCollection: String, parentId: String,
        subcollection: String, objectClass: Class<T>
    ): List<T>

    suspend fun deleteDocumentFromSubcollection(
        parentCollection: String, parentId: String, subcollection: String,
        documentId: String
    )

    fun getSubcollection(parentCollection: String, parentId: String, subcollection: String): CollectionReference
}

/**
 * Interface for reactive Flow-based data streams.
 */
interface IFlowActions {
    fun <T : Any> getCollectionGroupFlow(subcollectionName: String, objectClass: Class<T>): Flow<List<T>>
    fun <T : Any> getCollectionFlow(collectionPath: String, objectClass: Class<T>): Flow<List<T>>
}
