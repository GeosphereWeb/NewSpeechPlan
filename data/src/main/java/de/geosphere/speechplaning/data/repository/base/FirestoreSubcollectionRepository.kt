package de.geosphere.speechplaning.data.repository.base

/**
 * Generisches Interface für Firestore-Repositories, die auf Subcollections operieren.
 * @param T Der Typ der Entität in der Subcollection.
 * @param PID Der Typ der IDs der übergeordneten Dokumente. Oftmals String.
 *             (Die ID der Entität selbst ist hier implizit als String angenommen,
 *              basierend auf BaseFirestoreRepository, könnte aber auch generisch sein)
 */
interface FirestoreSubcollectionRepository<T : Any, PID : Any> { // Angenommen, Entitäts-ID bleibt String
    /**
     * Speichert eine Entität in einer Subcollection.
     * @param entity Die zu speichernde Entität.
     * @param parentIds Die IDs der übergeordneten Dokumente, die den Pfad zur Subcollection definieren.
     * @return Die ID der gespeicherten oder aktualisierten Entität.
     */
    suspend fun save(entity: T, vararg parentIds: PID): String // Entitäts-ID als String

    /**
     * Ruft eine Entität anhand ihrer ID aus einer Subcollection ab.
     * @param id Die ID der abzurufenden Entität.
     * @param parentIds Die IDs der übergeordneten Dokumente.
     * @return Die Entität oder null, wenn nicht gefunden.
     */
    suspend fun getById(id: String, vararg parentIds: PID): T?

    /**
     * Ruft alle Entitäten aus einer Subcollection ab.
     * @param parentIds Die IDs der übergeordneten Dokumente.
     * @return Eine Liste aller Entitäten.
     */
    suspend fun getAll(vararg parentIds: PID): List<T>

    /**
     * Löscht eine Entität anhand ihrer ID aus einer Subcollection.
     * @param id Die ID der zu löschenden Entität.
     * @param parentIds Die IDs der übergeordneten Dokumente.
     */
    suspend fun delete(id: String, vararg parentIds: PID)
}
