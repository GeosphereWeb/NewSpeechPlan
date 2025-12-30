package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.data.repository.services.IFirestoreService

/**
 * Generische Basisimplementierung für Firestore-Repositories.
 * @param T Der Typ der Entität. Muss ein '@DocumentId'-annotiertes Feld 'id: String' haben oder
 *          die Methode 'extractIdFromEntity' muss entsprechend überschrieben werden.
 * @param firestoreService Die Abstraktion über Firestore-Operationen.
 * @param collectionPath Der Pfad zur Firestore-Collection.
 * @param clazz Die Klassenreferenz der Entität (z.B. MyEntity::class.java).
 */
@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreRepository<T : Any>(
    protected val firestoreService: IFirestoreService,
    private val collectionPath: String,
    private val clazz: Class<T>
) : IFirestoreRepository<T, String> { // ID ist hier als String spezialisiert

    /**
     * Extrahiert die ID aus der Entität. Muss von Subklassen überschrieben werden,
     * um das korrekte ID-Feld der Entität zurückzugeben.
     * Z.B.: return entity.id
     */
    internal abstract fun extractIdFromEntity(entity: T): String

    /**
     * Prüft, ob die ID der Entität als "leer" oder "neu" betrachtet werden soll.
     * Standardmäßig wird String.isBlank() verwendet.
     */
    protected open fun isEntityIdBlank(id: String): Boolean {
        return id.isBlank()
    }

    override suspend fun save(entity: T): String {
        val entityId = extractIdFromEntity(entity)
        return try {
            if (isEntityIdBlank(entityId)) {
                firestoreService.saveDocument(collectionPath, entity)
            } else {
                val ok = firestoreService.saveDocumentWithId(collectionPath, entityId, entity)
                if (!ok) throw RuntimeException("Failed to set document with id $entityId in $collectionPath")
                entityId // Gibt die existierende ID zurück
            }
        } catch (e: Exception) {
            val idForErrorMessage = if (isEntityIdBlank(entityId)) "[new]" else entityId
            throw RuntimeException("Failed to save entity '$idForErrorMessage' in $collectionPath", e)
        }
    }

    override suspend fun getById(id: String): T? {
        return try {
            if (id.isBlank()) return null // Keine gültige ID zum Abrufen
            firestoreService.getDocument(collectionPath, id, clazz)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get entity '$id' from $collectionPath", e)
        }
    }

    override suspend fun getAll(): List<T> {
        return try {
            firestoreService.getDocuments(collectionPath, clazz)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get all entities from $collectionPath", e)
        }
    }

    override suspend fun delete(id: String) {
        // 1. Validate input first. This will throw IllegalArgumentException directly.
        require(id.isNotBlank()) { "Document ID cannot be blank for deletion." }

        // 2. Try the Firestore operation.
        try {
            firestoreService.deleteDocument(collectionPath, id)
        } catch (e: Exception) {
            // This catch block is now only for Firestore exceptions.
            throw RuntimeException("Failed to delete entity '$id' from $collectionPath", e)
        }
    }
}
