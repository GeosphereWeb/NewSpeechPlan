package de.geosphere.speechplaning.data.repository.services

import com.google.firebase.firestore.CollectionReference
import de.geosphere.speechplaning.core.model.SavableDataClass
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions")
interface IFirestoreService {

    // ... (bestehende Methoden bleiben unverändert) ...
    suspend fun <T> getDocument(collection: String, documentId: String, type: Class<T>): T?
    suspend fun <T> getDocuments(collection: String, type: Class<T>): List<T>
    suspend fun <T : SavableDataClass> saveDocument(collection: String, document: T): String
    fun <T> getCollection(clazz: Class<T>): CollectionReference

    /**
     * Gibt eine Referenz auf eine beliebige Subcollection zurück.
     *
     * @param parentCollection Der Name der übergeordneten Collection (z.B. "districts").
     * @param parentId Die ID des Dokuments in der übergeordneten Collection.
     * @param subcollection Der Name der Subcollection (z.B. "congregations").
     * @return Eine CollectionReference zur gewünschten Subcollection.
     */
    fun getSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String
    ): CollectionReference

    suspend fun <T : Any> saveDocumentWithId(collectionPath: String, documentId: String, document: T): Boolean

    // NEUE METHODEN FÜR SUBCOLLECTIONS
    suspend fun <T : Any> addDocumentToSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        data: T
    ): String

    suspend fun <T : Any> setDocumentInSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        documentId: String,
        data: T
    )

    suspend fun <T : Any> getDocumentsFromSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        objectClass: Class<T>
    ): List<T>

    suspend fun <T : Any> getDocumentFromSubcollection(
        parentCollectionPath: String,
        parentDocumentId: String,
        subcollectionName: String,
        documentId: String,
        objectClass: Class<T>
    ): T?

    suspend fun deleteDocumentFromSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        documentId: String
    )

    fun <T : Any> getCollectionGroupFlow(collectionId: String, type: Class<T>): Flow<List<T>>

    // Neu: Delete eines Dokuments in einer Top-Level-Collection
    suspend fun deleteDocument(collection: String, documentId: String)
}
