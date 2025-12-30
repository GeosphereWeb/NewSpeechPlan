package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.repository.services.ICollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import kotlinx.coroutines.flow.Flow

private const val SPEECHES_COLLECTION = "speeches"

@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
class SpeechRepositoryImpl(
    private val collectionActions: ICollectionActions,
    private val flowActions: IFlowActions
) {

    /**
     * Ruft alle aktiven Reden ab.
     * Dies filtert die Reden auf Client-Seite.
     * Für eine bessere Performance sollte dies eine serverseitige Query sein.
     *
     * @return Eine Liste von aktiven Speech-Objekten.
     */
    suspend fun getActiveSpeeches(): List<Speech> {
        return try {
            collectionActions.getDocuments(SPEECHES_COLLECTION, Speech::class.java)
                .filter { it.active }
        } catch (e: Exception) {
            throw RuntimeException("Failed to get active speech from $SPEECHES_COLLECTION", e)
        }
    }

    suspend fun deleteSpeech(speechId: String) {
        collectionActions.deleteDocument(SPEECHES_COLLECTION, speechId)
    }

    suspend fun getAllSpeeches(): List<Speech> {
        return try {
            collectionActions.getDocuments(SPEECHES_COLLECTION, Speech::class.java)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get all speech from $SPEECHES_COLLECTION", e)
        }
    }

    suspend fun saveSpeech(speech: Speech) {
        // Die Redenummer wird als Dokumenten-ID verwendet
        val speechToSave = speech.copy(id = speech.number)
        collectionActions.setDocument(SPEECHES_COLLECTION, speechToSave.id, speechToSave)
    }

    fun getAllSpeechesFlow(): Flow<List<Speech>> {
        return flowActions.getCollectionFlow(SPEECHES_COLLECTION, Speech::class.java)
    }
}
