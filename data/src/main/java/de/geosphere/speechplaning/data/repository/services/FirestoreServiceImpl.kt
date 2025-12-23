package de.geosphere.speechplaning.data.repository.services

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import de.geosphere.speechplaning.core.model.Chairman
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

const val TAG = "FirestoreServiceImpl"

@Suppress("TooManyFunctions", "TooGenericExceptionCaught", "TooGenericExceptionThrown")
class IFirestoreServiceImpl(private val firestore: FirebaseFirestore) : IFirestoreService {

    override suspend fun <T> getDocument(collection: String, documentId: String, type: Class<T>): T? {
        return try {
            val documentSnapshot = firestore.collection(collection).document(documentId).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(type)
            } else {
                null
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getDocument from collection '$collection', id '$documentId' failed: ", e)
            null
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "getDocument mapping error for collection '$collection', id '$documentId': ", e)
            null
        }
    }

    override suspend fun <T> getDocuments(collection: String, type: Class<T>): List<T> {
        return try {
            val querySnapshot = firestore.collection(collection).get().await()
            querySnapshot.documents.mapNotNull { it.toObject(type) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "getDocuments from collection '$collection' failed: ", e)
            emptyList()
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "getDocuments mapping error for collection '$collection': ", e)
            emptyList()
        }
    }

    override suspend fun <T : SavableDataClass> saveDocument(collection: String, document: T): String {
        return try {
            val documentReference = firestore.collection(collection).add(document).await()
            documentReference.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "saveDocument to collection '$collection' failed: ", e)
            ""
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "saveDocument invalid data for collection '$collection': ", e)
            ""
        }
    }

    override fun <T> getCollection(clazz: Class<T>): CollectionReference {
        return when (clazz) {
            Chairman::class.java -> firestore.collection("chairmen")
            Congregation::class.java -> firestore.collection("congregations")
            District::class.java -> firestore.collection("districts")
            Speaker::class.java -> {
                Log.w(
                    TAG,
                    "Speakers are a subcollection of Congregations." +
                        "Attempted to get Speaker collection as a top-level collection. "
                )
                firestore.collection("speakers_dummy_do_not_use")
            }

            Speech::class.java -> firestore.collection("speeches")
            else -> {
                Log.e(TAG, "Unknown class type for collection: ${clazz.simpleName}")
                firestore.collection("unknown_collection_${clazz.simpleName}")
            }
        }
    }

    override suspend fun <T : Any> saveDocumentWithId(
        collectionPath: String,
        documentId: String,
        document: T
    ): Boolean {
        return try {
            firestore.collection(collectionPath).document(documentId).set(document).await()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "saveDocumentWithId to '$collectionPath/$documentId' failed: ", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "saveDocumentWithId invalid data for '$collectionPath/$documentId': ", e)
            false
        }
    }

    // NEUE METHODEN FÜR SUBCOLLECTIONS
    override suspend fun <T : Any> addDocumentToSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        data: T
    ): String {
        return try {
            val documentReference =
                firestore.collection(parentCollection).document(parentId).collection(subcollection).add(data).await()
            documentReference.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding document to subcollection $subcollection in $parentCollection/$parentId", e)
            throw RuntimeException(
                "Error adding document to subcollection $subcollection in " + "$parentCollection/$parentId",
                e
            )
        }
    }

    override suspend fun <T : Any> setDocumentInSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        documentId: String,
        data: T
    ) {
        try {
            firestore.collection(parentCollection).document(parentId).collection(subcollection)
                .document(documentId)
                .set(data).await()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error setting document $documentId in subcollection $subcollection in " +
                    "$parentCollection/$parentId",
                e
            )
            throw RuntimeException(
                "Error setting document $documentId in subcollection $subcollection in " +
                    "$parentCollection/$parentId",
                e
            )
        }
    }

    override suspend fun <T : Any> getDocumentsFromSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        objectClass: Class<T>
    ): List<T> {
        return try {
            val querySnapshot =
                firestore.collection(parentCollection).document(parentId)
                    .collection(subcollection).get().await()
            querySnapshot.toObjects(objectClass)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error getting documents from subcollection $subcollection in $parentCollection/$parentId",
                e
            )
            throw RuntimeException(
                "Error getting documents from subcollection $subcollection in " +
                    "$parentCollection/$parentId",
                e
            )
        }
    }

    override suspend fun <T : Any> getDocumentFromSubcollection(
        parentCollectionPath: String,
        parentDocumentId: String,
        subcollectionName: String,
        documentId: String,
        objectClass: Class<T>
    ): T? {
        return try {
            if (parentDocumentId.isBlank() || documentId.isBlank()) return null
            val documentSnapshot =
                firestore.collection(parentCollectionPath).document(parentDocumentId).collection(subcollectionName)
                    .document(documentId).get().await()
            documentSnapshot.toObject(objectClass)
        } catch (e: FirebaseFirestoreException) {
            Log.e(
                TAG,
                "Error getting document '$documentId' from subcollection '$subcollectionName' " +
                    "under '$parentCollectionPath/$parentDocumentId'",
                e
            )
            throw RuntimeException(
                "Error getting document '$documentId' from subcollection '$subcollectionName' " +
                    "under '$parentCollectionPath/$parentDocumentId'",
                e
            )
        } catch (e: Exception) { // General fallback
            Log.e(
                TAG,
                "Unexpected error getting document '$documentId' from subcollection '$subcollectionName' " +
                    "under '$parentCollectionPath/$parentDocumentId'",
                e
            )
            throw RuntimeException(
                "Unexpected error getting document '$documentId' from subcollection '$subcollectionName' " +
                    "under '$parentCollectionPath/$parentDocumentId'",
                e
            )
        }
    }

    override suspend fun deleteDocumentFromSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        documentId: String
    ) {
        try {
            firestore.collection(parentCollection).document(parentId)
                .collection(subcollection).document(documentId)
                .delete().await()
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error deleting document $documentId from subcollection $subcollection in " +
                    "$parentCollection/$parentId",
                e
            )
            throw RuntimeException(
                "Error deleting document $documentId from subcollection $subcollection in " +
                    "$parentCollection/$parentId",
                e
            )
        }
    }

    // Bestehende spezifische Methoden für Subcollections (falls vorhanden und noch genutzt)
    suspend fun <T : Any> saveSubCollectionDocumentWithId(
        parentCollectionPath: String,
        parentId: String,
        subCollectionName: String,
        documentId: String,
        document: T
    ): Boolean {
        return try {
            firestore.collection(parentCollectionPath).document(parentId).collection(subCollectionName)
                .document(documentId).set(document).await()
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(
                TAG,
                "saveSubCollectionDocumentWithId to " +
                    "'$parentCollectionPath/$parentId/$subCollectionName/$documentId' failed: ",
                e
            )
            false
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "saveSubCollectionDocumentWithId invalid data for " +
                    "'$parentCollectionPath/$parentId/$subCollectionName/$documentId': ",
                e
            )
            false
        }
    }

    suspend fun <T : Any> addSubCollectionDocument(
        parentCollectionPath: String,
        parentId: String,
        subCollectionName: String,
        document: T
    ): String { // Gibt die ID des neuen Dokuments zurück
        return try {
            val docRef = firestore.collection(parentCollectionPath).document(parentId).collection(subCollectionName)
                .add(document).await()
            docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "addSubCollectionDocument to '$parentCollectionPath/$parentId/$subCollectionName' failed: ", e)
            ""
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "addSubCollectionDocument invalid data for '$parentCollectionPath/$parentId/$subCollectionName': ",
                e
            )
            ""
        }
    }

    override fun getSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String
    ): CollectionReference {
        require(parentId.isNotBlank()) {
            "Parent document ID cannot be blank when accessing a subcollection."
        }
        // Baut den Pfad dynamisch zusammen: parentCollection/{parentId}/subcollection
        return firestore.collection(parentCollection).document(parentId).collection(subcollection)
    }

    override fun <T : Any> getCollectionGroupFlow(collectionId: String, type: Class<T>): Flow<List<T>> = callbackFlow {
        val registration = firestore.collectionGroup(collectionId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val objects = snapshot.toObjects(type)
                trySend(objects)
                // Debug-Logging: Anzahl der Dokumente und Beispiel-IDs (falls vorhanden)
                try {
                    val ids = snapshot.documents.mapNotNull { it.id }.take(5)
                    Log.d(TAG, "collectionGroup('$collectionId') snapshot received: size=${objects.size}, ids=$ids")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to log collectionGroup snapshot details", e)
                }
            }
        }
        awaitClose { registration.remove() }
    }

    // Neu: Implementierung für Top-Level-Delete
    override suspend fun deleteDocument(collection: String, documentId: String) {
        try {
            require(documentId.isNotBlank()) { "Document ID cannot be blank when deleting a document." }
            firestore.collection(collection).document(documentId).delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "deleteDocument failed for '$collection/$documentId'", e)
            throw RuntimeException("Failed to delete document '$documentId' in collection '$collection'", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error deleting document '$documentId' in collection '$collection'", e)
            throw RuntimeException("Unexpected error deleting document '$documentId' in collection '$collection'", e)
        }
    }
}
