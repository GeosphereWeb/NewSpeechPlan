package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.data.services.FirestoreService

/**
 * Abstrakte Basisklasse für Firestore-Repositories, die mit Subcollections arbeiten.
 * Implementiert das [FirestoreSubcollectionRepository] Interface, wobei der Typ
 * der Parent-IDs auf [String] spezialisiert wird.
 *
 * @param T Der Typ der Entität in der Subcollection.
 * @param firestoreService Die Instanz des FirestoreService.
 * @param subcollectionName Der Name der Subcollection (z.B. "speakers", "congregations").
 * @param clazz Die Klassenreferenz der Entität (z.B. MyEntity::class.java).
 */
@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class BaseFirestoreSubcollectionRepository<T : Any>(
    protected val firestoreService: FirestoreService,
    private val subcollectionName: String,
    private val clazz: Class<T>
) : FirestoreSubcollectionRepository<T, String> { // <--- HIER wird PID auf String spezialisiert

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

    /**
     * Erstellt den Pfad zur Collection, die das Elterndokument enthält.
     * @param parentIds Die IDs der übergeordneten Dokumente (hier als Strings).
     *                  Die Reihenfolge und Bedeutung hängt von der Implementierung der Subklasse ab.
     */
    internal abstract fun buildParentCollectionPath(vararg parentIds: String): String

    /**
     * Ermittelt die ID des direkten Elterndokuments der Subcollection.
     * @param parentIds Die IDs der übergeordneten Dokumente (hier als Strings).
     */
    internal abstract fun getParentDocumentId(vararg parentIds: String): String

    override suspend fun save(entity: T, vararg parentIds: String): String {
        val entityId = extractIdFromEntity(entity)
        // Da parentIds hier schon String sind, ist keine Konvertierung von PID nötig.
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)

        return try {
            if (isEntityIdBlank(entityId)) {
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
                entityId
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

    override suspend fun getById(id: String, vararg parentIds: String): T? {
        if (id.isBlank()) return null
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)
        return try {
            firestoreService.getDocumentFromSubcollection(
                parentCollectionPath = actualParentCollectionPath,
                parentDocumentId = actualParentDocumentId,
                subcollectionName = subcollectionName,
                documentId = id, // 'id' ist hier die Entitäts-ID, nicht verwechseln mit parentIds
                objectClass = clazz
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to get entity '$id' from subcollection '$subcollectionName' under " +
                    "parent '$actualParentDocumentId' in '$actualParentCollectionPath'",
                e
            )
        }
    }

    override suspend fun getAll(vararg parentIds: String): List<T> {
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

    override suspend fun delete(id: String, vararg parentIds: String) {
        require(id.isNotBlank()) { "Document ID cannot be blank for deletion." }
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)
        try {
            firestoreService.deleteDocumentFromSubcollection(
                parentCollection = actualParentCollectionPath,
                parentId = actualParentDocumentId,
                subcollection = subcollectionName,
                documentId = id // 'id' ist hier die Entitäts-ID
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "Failed to delete entity '$id' from subcollection " +
                    "'$subcollectionName' under parent '$actualParentDocumentId' in '$actualParentCollectionPath'",
                e
            )
        }
    }
}
