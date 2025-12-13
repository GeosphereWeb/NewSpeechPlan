package de.geosphere.speechplaning.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.base.FirestoreRepositoryImpl
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val DISTRICT_COLLECTION = "districts"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class DistrictRepositoryImpl(
    firestore: FirebaseFirestore
) : FirestoreRepositoryImpl<District>(
    firestore = firestore,
    collectionPath = DISTRICT_COLLECTION,
    clazz = District::class.java
) {

    // Implementierung der abstrakten Methode aus BaseFirestoreRepository
    override fun extractIdFromEntity(entity: District): String {
        return entity.id
    }

    /**
     * Ruft alle aktiven Reden ab.
     * Dies verwendet eine serverseitige Query.
     * Diese Methode ist spezifisch für SpeechRepository und nicht Teil der Basisklasse.
     *
     * @return Eine Liste von aktiven Speech-Objekten.
     */
    suspend fun getActiveDistricts(): List<District> {
        return try {
            val querySnapshot = firestore.collection(DISTRICT_COLLECTION)
                .whereEqualTo("active", true)
                .get()
                .await()
            querySnapshot.toObjects(District::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get active district from $DISTRICT_COLLECTION", e)
        }
    }

    suspend fun deleteDistrict(id: String) {
        delete(id)
    }

    suspend fun getAllDistrict(): List<District> {
        return try {
            val querySnapshot = firestore.collection(DISTRICT_COLLECTION)
                .get()
                .await()
            querySnapshot.toObjects(District::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get all district from $DISTRICT_COLLECTION", e)
        }
    }

    suspend fun saveDistrict(district: District) {
        save(district)
    }

    fun getAllDistrictFlow(): Flow<List<District>> = callbackFlow {
        // 1. Listener bei Firestore registrieren
        val listenerRegistration = firestore.collection(DISTRICT_COLLECTION)
            .addSnapshotListener { snapshot, error ->

                // Fehlerbehandlung: Wenn Firestore einen Fehler meldet, den Flow schließen
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // Erfolgsfall: Daten mappen und emittieren
                if (snapshot != null) {
                    val district = snapshot.toObjects(District::class.java)
                    // trySend versucht, den Wert in den Flow zu schieben (thread-safe)
                    trySend(district)
                }
            }

        // 2. Cleanup: Wird aufgerufen, wenn der Flow cancelled wird (z.B. ViewModel cleared)
        // Das verhindert Memory Leaks, weil der Listener entfernt wird.
        awaitClose {
            listenerRegistration.remove()
        }
    }
}
