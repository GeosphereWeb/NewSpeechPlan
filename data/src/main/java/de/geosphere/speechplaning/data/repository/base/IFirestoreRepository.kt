package de.geosphere.speechplaning.data.repository.base

import kotlinx.coroutines.flow.Flow

/**
 * Generisches Interface für Firestore-Repositories.
 * @param T Der Typ der Entität.
 * @param ID Der Typ des Primärschlüssels/Dokument-IDs der Entität (typischerweise String).
 */
interface IFirestoreRepository<T : Any, ID : Any> {
    /**
     * Speichert eine Entität. Wenn die ID der Entität nicht existiert oder als "neu" erkannt wird,
     * wird ein neues Dokument erstellt und dessen ID zurückgegeben.
     * Andernfalls wird das bestehende Dokument aktualisiert und dessen ID zurückgegeben.
     *
     * @param entity Die zu speichernde Entität.
     * @return Die ID der gespeicherten oder aktualisierten Entität.
     */
    suspend fun save(entity: T): ID

    /**
     * Ruft eine Entität anhand ihrer ID ab.
     *
     * @param id Die ID der abzurufenden Entität.
     * @return Die Entität oder null, wenn nicht gefunden.
     */
    suspend fun getById(id: ID): T?

    /**
     * Löscht eine Entität anhand ihrer ID.
     *
     * @param id Die ID der zu löschenden Entität.
     */
    suspend fun delete(id: ID)

    fun getAllFlow(): Flow<List<T>>
}
