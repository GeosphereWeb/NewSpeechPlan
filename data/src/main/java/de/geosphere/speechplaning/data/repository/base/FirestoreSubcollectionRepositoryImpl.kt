package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.data.repository.services.IFirestoreService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Abstrakte Basisklasse für Firestore-Repositories, die mit Subcollections arbeiten.
 * Implementiert das [IFirestoreSubcollectionRepository] Interface, wobei der Typ
 * der Parent-IDs auf [String] spezialisiert wird.
 *
 * @param T Der Typ der Entität in der Subcollection.
 * @param firestoreService Die Instanz des FirestoreService.
 * @param subcollectionName Der Name der Subcollection (z.B. "speakers", "congregations").
 * @param clazz Die Klassenreferenz der Entität (z.B. MyEntity::class.java).
 */
@Suppress("TooGenericExceptionCaught", "TooGenericExceptionThrown")
abstract class FirestoreSubcollectionRepositoryImpl<T : Any>(
    protected val firestoreService: IFirestoreService,
    private val subcollectionName: String,
    private val clazz: Class<T>
) : IFirestoreSubcollectionRepository<T, String> { // <--- HIER wird PID auf String spezialisiert

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

    /**
     * Ruft alle Entitäten aus der Subcollection als Flow ab, der bei Änderungen automatisch aktualisiert wird.
     *
     * @param parentIds Die IDs der übergeordneten Dokumente, die benötigt werden, um den Pfad zu bilden.
     * @return Ein Flow, der eine Liste von Entitäten emittiert.
     */
    override fun getAllFlow(vararg parentIds: String): Flow<List<T>> = callbackFlow {
        val actualParentCollectionPath = buildParentCollectionPath(*parentIds)
        val actualParentDocumentId = getParentDocumentId(*parentIds)

        // Wir nutzen die generische Service-Methode, die wir zuvor erstellt haben.
        val collectionRef = firestoreService.getSubcollection(
            parentCollection = actualParentCollectionPath,
            parentId = actualParentDocumentId,
            subcollection = subcollectionName
        )

        // Die Listener-Logik ist jetzt an einer zentralen, wiederverwendbaren Stelle.
        val listenerRegistration = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error) // Beendet den Flow mit einem Fehler
                return@addSnapshotListener
            }
            if (snapshot != null) {
                // Die Klasse 'T' und 'clazz' sind dank Generics und Konstruktor hier bekannt.
                val data = snapshot.toObjects(clazz)
                trySend(data) // Sendet die neue Liste an den Flow
            }
        }

        // Wird aufgerufen, wenn der Flow beendet wird -> räumt den Listener auf
        awaitClose {
            listenerRegistration.remove()
        }
    }
}
