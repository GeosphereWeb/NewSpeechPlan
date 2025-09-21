package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.data.services.FirestoreService

/**
 * Abstrakte Basisklasse für Firestore-Repositories, die mit Subcollections arbeiten.
 *
 * @param T Der Typ der Entität in der Subcollection.
 * @param firestoreService Die Instanz des FirestoreService.
 * @param subcollectionName Der Name der Subcollection (z.B. \"speakers\", \"congregations\").
 * @param clazz Die Klassenreferenz der Entität (z.B. MyEntity::class.java).
 */
@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class BaseFirestoreSubcollectionRepository<T : Any>(
    protected val firestoreService: FirestoreService,
    private val subcollectionName: String,
    private val clazz: Class<T>
) {

    /**
     * Extrahiert die ID aus der Entität. Muss von Subklassen überschrieben werden,
     * um das korrekte ID-Feld der Entität zurückzugeben.
     * Z.B.: return entity.id
     */
    internal abstract fun extractIdFromEntity(entity: T): String

    /**
     * Prüft, ob die ID der Entität als \"leer\" oder \"neu\" betrachtet werden soll.
     * Standardmäßig wird String.isBlank() verwendet.
     */
    protected open fun isEntityIdBlank(id: String): Boolean {
        return id.isBlank()
    }

    /**
     * Erstellt den Pfad zur Collection, die das Elterndokument enthält.
     * @param parentIds Die IDs der übergeordneten Dokumente, die zur Pfadkonstruktion benötigt werden.
     *                  Die Reihenfolge und Bedeutung hängt von der Implementierung der Subklasse ab.
     *                  Beispiel für Speaker (districtId, congregationId): \"districts/${parentIds[0]}/congregations\"
     *                  Beispiel für Congregation (districtId): \"districts\"
     */
    internal abstract fun buildParentCollectionPath(vararg parentIds: String): String

    /**
     * Ermittelt die ID des direkten Elterndokuments der Subcollection.
     * @param parentIds Die IDs der übergeordneten Dokumente.
     *                  Beispiel für Speaker (districtId, congregationId): parentIds[1] (congregationId)
     *                  Beispiel für Congregation (districtId): parentIds[0] (districtId)
     */
    internal abstract fun getParentDocumentId(vararg parentIds: String): String

    /**
     * Speichert eine Entität in der Subcollection. Wenn die Entität bereits eine ID hat,
     * wird sie aktualisiert, ansonsten wird ein neues Dokument erstellt.
     *
     * @param entity Die zu speichernde Entität.
     * @param parentIds Die IDs der übergeordneten Dokumente, die zur Pfadkonstruktion benötigt werden.
     * @return Die ID der gespeicherten Entität (entweder die existierende oder die neu generierte).
     */
    suspend fun save(entity: T, vararg parentIds: String): String {
        val entityId = extractIdFromEntity(entity)
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)

        return try {
            if (isEntityIdBlank(entityId)) {
                // FirestoreService.addDocumentToSubcollection gibt die neue ID zurück
                firestoreService.addDocumentToSubcollection(
                    parentCollection = actualParentCollectionPath,
                    parentId = actualParentDocumentId,
                    subcollection = subcollectionName,
                    data = entity
                )
            } else {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = actualParentCollectionPath,
                    parentId = actualParentDocumentId,
                    subcollection = subcollectionName,
                    documentId = entityId,
                    data = entity
                )
                entityId // Gibt die existierende ID zurück
            }
        } catch (e: Exception) {
            val idForErrorMessage = if (isEntityIdBlank(entityId)) "[new]" else entityId
            throw RuntimeException(
                "Failed to save entity '$idForErrorMessage' in subcollection " +
                    "'$subcollectionName' under parent '$actualParentDocumentId' in '$actualParentCollectionPath'",
                e
            )
        }
    }

    /**
     * Ruft alle Entitäten aus der Subcollection ab.
     *
     * @param parentIds Die IDs der übergeordneten Dokumente.
     * @return Eine Liste von Entitäten des Typs T.
     */
    suspend fun getAll(vararg parentIds: String): List<T> {
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)
        return try {
            firestoreService.getDocumentsFromSubcollection(
                parentCollection = actualParentCollectionPath,
                parentId = actualParentDocumentId,
                subcollection = subcollectionName,
                objectClass = clazz
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get all entities from subcollection '$subcollectionName' under " +
                    "parent '$actualParentDocumentId' in '$actualParentCollectionPath'",
                e
            )
        }
    }

    /**
     * Ruft eine spezifische Entität anhand ihrer ID aus der Subcollection ab.
     *
     * @param entityId Die ID der abzurufenden Entität.
     * @param parentIds Die IDs der übergeordneten Dokumente.
     * @return Die gefundene Entität oder null, wenn nicht vorhanden.
     */
    suspend fun getById(entityId: String, vararg parentIds: String): T? {
        if (entityId.isBlank()) return null
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)
        return try {
            firestoreService.getDocumentFromSubcollection(
                parentCollectionPath = actualParentCollectionPath,
                parentDocumentId = actualParentDocumentId,
                subcollectionName = subcollectionName,
                documentId = entityId,
                objectClass = clazz
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$entityId' from subcollection '$subcollectionName' under " +
                    "parent '$actualParentDocumentId' in '$actualParentCollectionPath'",
                e
            )
        }
    }

    /**
     * Löscht eine Entität anhand ihrer ID aus der Subcollection.
     *
     * @param entityId Die ID der zu löschenden Entität.
     * @param parentIds Die IDs der übergeordneten Dokumente.
     */
    suspend fun delete(entityId: String, vararg parentIds: String) {
        require(entityId.isNotBlank()) { "Document ID cannot be blank for deletion." }
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)
        try {
            firestoreService.deleteDocumentFromSubcollection(
                parentCollection = actualParentCollectionPath,
                parentId = actualParentDocumentId,
                subcollection = subcollectionName,
                documentId = entityId
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$entityId' from subcollection " +
                    "'$subcollectionName' under parent '$actualParentDocumentId' in '$actualParentCollectionPath'",
                e
            )
        }
    }
}
