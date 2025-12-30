package de.geosphere.speechplaning.data.repository.services

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class FirestoreServiceImpl(
    private val firestore: FirebaseFirestore
) : IFirestoreService {

    override suspend fun <T : Any> addDocument(collectionPath: String, data: T): String {
        val ref = firestore.collection(collectionPath).add(data).await()
        return ref.id
    }

    override suspend fun <T : Any> setDocument(collectionPath: String, documentId: String, data: T) {
        firestore.collection(collectionPath).document(documentId).set(data).await()
    }

    override suspend fun <T : Any> getDocument(collectionPath: String, documentId: String, objectClass: Class<T>): T? {
        if (documentId.isBlank()) return null
        val snap = firestore.collection(collectionPath).document(documentId).get().await()
        return snap.toObject(objectClass)
    }

    override suspend fun <T : Any> getDocuments(collectionPath: String, objectClass: Class<T>): List<T> {
        val snap = firestore.collection(collectionPath).get().await()
        return snap.toObjects(objectClass)
    }

    override suspend fun deleteDocument(collectionPath: String, documentId: String) {
        require(documentId.isNotBlank()) { "Document ID cannot be blank for deletion." }
        firestore.collection(collectionPath).document(documentId).delete().await()
    }

    override suspend fun <T : Any> addDocumentToSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        data: T
    ): String {
        val ref = firestore.collection(parentCollection)
            .document(parentId)
            .collection(subcollection)
            .add(data)
            .await()
        return ref.id
    }

    override suspend fun <T : Any> setDocumentInSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        documentId: String,
        data: T
    ) {
        firestore.collection(parentCollection)
            .document(parentId)
            .collection(subcollection)
            .document(documentId)
            .set(data)
            .await()
    }

    override suspend fun <T : Any> getDocumentFromSubcollection(
        parentCollectionPath: String,
        parentDocumentId: String,
        subcollectionName: String,
        documentId: String,
        objectClass: Class<T>
    ): T? {
        if (documentId.isBlank()) return null
        val snap = firestore.collection(parentCollectionPath)
            .document(parentDocumentId)
            .collection(subcollectionName)
            .document(documentId)
            .get()
            .await()
        return snap.toObject(objectClass)
    }

    override suspend fun <T : Any> getDocumentsFromSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        objectClass: Class<T>
    ): List<T> {
        // Wenn parentCollection oder parentId leer sind, versuchen wir eine CollectionGroup-Query
        return if (parentCollection.isBlank() || parentId.isBlank()) {
            val snap = firestore.collectionGroup(subcollection).get().await()
            snap.toObjects(objectClass)
        } else {
            val snap = firestore.collection(parentCollection)
                .document(parentId)
                .collection(subcollection)
                .get()
                .await()
            snap.toObjects(objectClass)
        }
    }

    override suspend fun deleteDocumentFromSubcollection(
        parentCollection: String,
        parentId: String,
        subcollection: String,
        documentId: String
    ) {
        require(documentId.isNotBlank()) { "Document ID cannot be blank for deletion." }
        firestore.collection(parentCollection)
            .document(parentId)
            .collection(subcollection)
            .document(documentId)
            .delete()
            .await()
    }

    override fun getSubcollection(parentCollection: String, parentId: String, subcollection: String): CollectionReference {
        return firestore.collection(parentCollection).document(parentId).collection(subcollection)
    }

    // Project-specific helpers
    override suspend fun <T : Any> saveDocument(collectionPath: String, data: T): String {
        return addDocument(collectionPath, data)
    }

    override suspend fun <T : Any> saveDocumentWithId(collectionPath: String, documentId: String, data: T): Boolean {
        return try {
            setDocument(collectionPath, documentId, data)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun <T : Any> getCollectionGroupFlow(subcollectionName: String, objectClass: Class<T>): Flow<List<T>> = callbackFlow {
        val listener = firestore.collectionGroup(subcollectionName).addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            if (snap != null) {
                val data = snap.toObjects(objectClass)
                trySend(data)
            }
        }

        awaitClose { listener.remove() }
    }

    override fun <T : Any> getCollectionFlow(collectionPath: String, objectClass: Class<T>): Flow<List<T>> = callbackFlow {
        val listener = firestore.collection(collectionPath).addSnapshotListener { snap, err ->
            if (err != null) {
                close(err)
                return@addSnapshotListener
            }
            if (snap != null) {
                val data = snap.toObjects(objectClass)
                trySend(data)
            }
        }

        awaitClose { listener.remove() }
    }
}
