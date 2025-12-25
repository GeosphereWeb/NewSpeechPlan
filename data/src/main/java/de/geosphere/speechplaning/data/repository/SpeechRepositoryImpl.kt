package de.geosphere.speechplaning.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.repository.base.FirestoreRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val SPEECHES_COLLECTION = "speeches"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class SpeechRepositoryImpl(
    firestore: FirebaseFirestore
) : FirestoreRepository<Speech>(
    firestore = firestore,
    collectionPath = SPEECHES_COLLECTION,
    clazz = Speech::class.java
) {

    // Implementierung der abstrakten Methode aus BaseFirestoreRepository
    override fun extractIdFromEntity(entity: Speech): String {
        return entity.id
    }

    /**
     * Ruft alle aktiven Reden ab.
     * Dies verwendet eine serverseitige Query.
     * Diese Methode ist spezifisch für SpeechRepository und nicht Teil der Basisklasse.
     *
     * @return Eine Liste von aktiven Speech-Objekten.
     */
    suspend fun getActiveSpeeches(): List<Speech> {
        return try {
            val querySnapshot = firestore.collection(SPEECHES_COLLECTION)
                .whereEqualTo("active", true)
                .get()
                .await()
            querySnapshot.toObjects(Speech::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get active speech from $SPEECHES_COLLECTION", e)
        }
    }

    suspend fun deleteSpeech(speechId: String) {
        delete(speechId)
    }

    suspend fun getAllSpeeches(): List<Speech> {
        return try {
            val querySnapshot = firestore.collection(SPEECHES_COLLECTION)
                .get()
                .await()
            querySnapshot.toObjects(Speech::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get all speech from $SPEECHES_COLLECTION", e)
        }
    }

    suspend fun saveSpeech(speech: Speech) {
        save(speech.copy(id = speech.number))
    }

    fun getAllSpeechesFlow(): Flow<List<Speech>> = callbackFlow {
        // 1. Listener bei Firestore registrieren
        val listenerRegistration = firestore.collection(SPEECHES_COLLECTION)
            .addSnapshotListener { snapshot, error ->

                // Fehlerbehandlung: Wenn Firestore einen Fehler meldet, den Flow schließen
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Erfolgsfall: Daten mappen und emittieren
                if (snapshot != null) {
                    val speeches = snapshot.toObjects(Speech::class.java)
                    // trySend versucht, den Wert in den Flow zu schieben (thread-safe)
                    trySend(speeches)
                }
            }

        // 2. Cleanup: Wird aufgerufen, wenn der Flow cancelled wird (z.B. ViewModel cleared)
        // Das verhindert Memory Leaks, weil der Listener entfernt wird.
        awaitClose {
            listenerRegistration.remove()
        }
    }
}
