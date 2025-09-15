package de.geosphere.speechplaning.data.repository.base

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

// Test-Entität für die Tests
data class TestEntity(
    val id: String = "",
    val name: String = "",
    val value: Int = 0,
    val active: Boolean = false
)

// Konkrete Implementierung der abstrakten Klasse für Tests
class TestFirestoreRepository(
    firestore: FirebaseFirestore,
    collectionPath: String = "test-collection"
) : BaseFirestoreRepository<TestEntity>(firestore, collectionPath, TestEntity::class.java) {

    override fun extractIdFromEntity(entity: TestEntity): String = entity.id
}

class BaseFirestoreRepositoryTest {

    private val firestoreMock: FirebaseFirestore = mockk()
    private val collectionReferenceMock: CollectionReference = mockk()
    private val documentReferenceMock: DocumentReference = mockk()
    private val queryMock: Query = mockk()

    // Tasks Mocks - Using relaxed = true to avoid mocking all Task methods
    private val voidTaskMock: Task<Void> = mockk(relaxed = true)
    private val documentReferenceTaskMock: Task<DocumentReference> = mockk(relaxed = true)
    private val querySnapshotTaskMock: Task<QuerySnapshot> = mockk(relaxed = true)
    private val documentSnapshotTaskMock: Task<DocumentSnapshot> = mockk(relaxed = true)
    private lateinit var testFirestoreRepository: TestFirestoreRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this) // Initialize @MockK annotated mocks

        every { firestoreMock.collection(any()) } returns collectionReferenceMock
        every { collectionReferenceMock.document(any()) } returns documentReferenceMock
        every { collectionReferenceMock.add(any()) } returns documentReferenceTaskMock
        every { documentReferenceMock.set(any()) } returns voidTaskMock
        every { documentReferenceMock.get() } returns documentSnapshotTaskMock
        every { documentReferenceMock.delete() } returns voidTaskMock
        every { collectionReferenceMock.get() } returns querySnapshotTaskMock
        every { collectionReferenceMock.whereEqualTo(any<String>(), any()) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock

        testFirestoreRepository = TestFirestoreRepository(firestoreMock)
    }

    // Helper zum einfachen Mocken von Task-Ergebnissen für await()
    private fun <T> mockTaskResult(task: Task<T>, resultData: T?, exception: Exception? = null) {
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        if (exception != null) {
            every { task.isSuccessful } returns false
            every { task.exception } returns exception
        } else {
            every { task.isSuccessful } returns true
            every { task.exception } returns null
            every { task.result } returns resultData
        }
    }

    @Test
    fun `extractIdFromEntity should return correct id`() {
        val district = TestEntity(id = "testId", name = "Test District")
        val extractedId = testFirestoreRepository.extractIdFromEntity(district)
        assertEquals("testId", extractedId)
    }

    @Test
    fun `save new testEntity should add to firestore and return new id`() = runTest {
        val newTestEntity = TestEntity(id = "", name = "Max Mustermann", active = true)
        val generatedId = "generatedFirebaseId"
        val addedDocRefMock: DocumentReference = mockk() // separater Mock für das Ergebnis von add()

        mockTaskResult(documentReferenceTaskMock, addedDocRefMock)
        every { addedDocRefMock.id } returns generatedId

        val resultId = testFirestoreRepository.save(newTestEntity)

        verify { collectionReferenceMock.add(newTestEntity) }
        assertEquals(generatedId, resultId)
    }

    /**
     * ### Erklärung des neuen Tests:
     *
     * 1.  **Arrange:**
     *     *   Wir erstellen eine `existingEntity` mit einer nicht-leeren ID (`"existingId"`).
     *     *   Wir simulieren eine `RuntimeException`, die beim `.set()`-Aufruf auftreten soll.
     *     *   Wir mocken die Aufrufkette: `document("existingId")` -> `set(existingEntity)` -> `voidTaskMock`.
     *     *   Mit `mockTaskResult(voidTaskMock, null, simulatedException)` sorgen wir dafür, dass der `await()`
     *     *    -Aufruf auf den `.set()`-Task fehlschlägt.
     *
     * 2.  **Act & Assert:**
     *     *   Wir rufen `testFirestoreRepository.save(existingEntity)` innerhalb von `assertThrows` auf.
     *     *   Wir fangen die `RuntimeException` auf und prüfen die Nachricht. Diesmal muss die Nachricht die
     *     *   `existingId` enthalten und nicht `"[new]"`, wodurch der **else-Branch** getestet wird.
     *     *   Wir prüfen auch hier die `cause`.
     *
     * 3.  **Verify:**
     *     *   Wir stellen sicher, dass der Pfad für das Update (`document()` und `set()`) und nicht der für das
     *     *    Hinzufügen (`add()`) aufgerufen wurde.
     *
     *
     */
    @Test
    fun `save existing testEntity should throw runtime exception on firestore failure`() = runTest {
        // Arrange: Bereite eine existierende Entität und einen Fehlerfall vor
        val existingEntity = TestEntity(id = "existingId", name = "Existing", active = true)
        val simulatedException = RuntimeException("Simulated Firestore set failure")

        // Mocke die Kette für einen fehlschlagenden 'set'-Aufruf (Update)
        every { collectionReferenceMock.document(existingEntity.id) } returns documentReferenceMock
        every { documentReferenceMock.set(existingEntity) } returns voidTaskMock
        mockTaskResult(voidTaskMock, null, simulatedException) // Simuliere, dass der Task fehlschlägt

        // Act & Assert: Führe die save-Methode aus und erwarte eine Exception
        val exception = assertThrows<RuntimeException> {
            testFirestoreRepository.save(existingEntity)
        }

        // Überprüfe, ob die Exception die korrekte Nachricht (mit der ID) und Ursache hat
        val expectedMessage = "Failed to save entity '${existingEntity.id}' in test-collection"
        assertTrue(
            exception.message?.contains(expectedMessage) ?: false,
            "Exception message was not as expected."
        )
        assertEquals(
            simulatedException,
            exception.cause,
            "The cause of the exception was not the simulated one."
        )

        // Verify: Stelle sicher, dass die korrekten Methoden aufgerufen wurden
        verify(exactly = 1) { collectionReferenceMock.document(existingEntity.id) }
        verify(exactly = 1) { documentReferenceMock.set(existingEntity) }
    }

    @Test
    fun `save new testEntity should throw runtime exception on firestore failure`() = runTest {
        val newEntity = TestEntity(id = "", name = "Max Mustermann", active = true)
        val simpleException = RuntimeException("Simulated Firestore error")

        // Task schlägt fehl
        mockTaskResult(documentReferenceTaskMock, null, simpleException)
        // Sicherstellen, dass add mit newSpeech den fehlschlagenden Task zurückgibt
        every { collectionReferenceMock.add(newEntity) } returns documentReferenceTaskMock

        val exception = assertThrows<RuntimeException> {
            testFirestoreRepository.save(newEntity)
        }

        assertTrue(exception.message?.contains("Failed to save entity '[new]' in test-collection") ?: false)
        assertEquals(simpleException, exception.cause)
    }

    @Test
    fun `save existing testEntity should set document in firestore and return existing id`() = runTest {
        val existingDistrict = TestEntity(
            id = "existingId",
            name = "Max Mustermann",
            active = true
        )
        mockTaskResult(voidTaskMock, null) // Für Task<Void> ist das Ergebnis null

        val resultId = testFirestoreRepository.save(existingDistrict)

        verify { collectionReferenceMock.document("existingId") }
        verify { documentReferenceMock.set(existingDistrict) }
        assertEquals("existingId", resultId)
    }

    @Test
    fun `getById with valid id should return testEntity object`() = runTest {
        val testEntityId = "entity123"
        val expectedDistrict = TestEntity(
            id = testEntityId,
            name = "Max Mustermann",
            active = true
        )
        val snapshotResultMock: DocumentSnapshot = mockk()

        mockTaskResult(documentSnapshotTaskMock, snapshotResultMock)
        every { snapshotResultMock.exists() } returns true
        every { snapshotResultMock.toObject(TestEntity::class.java) } returns expectedDistrict
        // Sicherstellen, dass der richtige docRef für get verwendet wird:
        every { collectionReferenceMock.document(testEntityId) } returns documentReferenceMock
        every { documentReferenceMock.get() } returns documentSnapshotTaskMock

        val result = testFirestoreRepository.getById(testEntityId)

        assertNotNull(result)
        assertEquals(expectedDistrict, result)
        verify { collectionReferenceMock.document(testEntityId) }
    }

    @Test
    fun `getById() with invalid id should return null`() = runTest {
        val testEntityId = "nonExistingId"
        val snapshotResultMock: DocumentSnapshot = mockk()

        mockTaskResult(documentSnapshotTaskMock, snapshotResultMock)
        every { snapshotResultMock.exists() } returns false
        every { snapshotResultMock.toObject(TestEntity::class.java) } returns null
        every { collectionReferenceMock.document(testEntityId) } returns documentReferenceMock
        every { documentReferenceMock.get() } returns documentSnapshotTaskMock

        val result = testFirestoreRepository.getById(testEntityId)

        assertNull(result)
        verify { collectionReferenceMock.document(testEntityId) }
    }

    @Test
    fun `getById() should throw runtime exception on firestore failure`() = runTest {
        // Arrange
        val entityId = "entity123"
        val simulatedException = RuntimeException("Simulated Firestore error")

        // Mocke die Kette für einen fehlschlagenden getById-Aufruf
        every { collectionReferenceMock.document(entityId) } returns documentReferenceMock
        every { documentReferenceMock.get() } returns documentSnapshotTaskMock
        mockTaskResult(documentSnapshotTaskMock, null, simulatedException)

        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            testFirestoreRepository.getById(entityId)
        }

        // Überprüfe die Exception
        val expectedMessage = "Failed to get entity '$entityId' from test-collection"
        Assertions.assertTrue(
            exception.message?.contains(expectedMessage) ?: false,
            "Exception message was not as expected."
        )
        assertEquals(
            simulatedException,
            exception.cause,
            "Exception cause was not the simulated one."
        )

        // Verify
        verify(exactly = 1) { collectionReferenceMock.document(entityId) }
        verify(exactly = 1) { documentReferenceMock.get() }
    }

    /**
     *
     * ### Erklärung des neuen Tests:
     *
     * 1.  **Arrange:** Wir definieren eine `blankId`. Ein leerer String `""` würde genauso gut funktionieren.
     * 2.  **Act:** Wir rufen `testFirestoreRepository.getById()` mit dieser `blankId` auf.
     * 3.  **Assert:** Wir überprüfen mit `assertNull()`, ob das Ergebnis wie erwartet `null` ist. Dies deckt
     *      *   den `true`-Pfad der `if (id.isBlank())`-Bedingung ab.
     * 4.  **Verify:** Dieser Schritt ist entscheidend für die Qualität des Tests. Mit `verify(exactly = 0)`
     *      *   stellen wir sicher, dass **keine** Interaktion mit Firestore stattfindet. Das beweist, dass deine
     *      *   "Guard Clause" (die `if`-Abfrage) funktioniert und unnötige Datenbankaufrufe verhindert.
     *
     */
    @Test
    fun `getById with blank id should return null immediately`() = runTest {
        // Arrange
        val blankId = "   " // Eine leere oder nur aus Leerzeichen bestehende ID

        // Act
        // Rufe getById mit der leeren ID auf
        val result = testFirestoreRepository.getById(blankId)

        // Assert
        // Das Ergebnis muss null sein, wie in der if-Bedingung definiert.
        assertNull(result, "Sollte sofort null zurückgeben, wenn die ID leer ist.")

        // Verify
        // Sehr wichtig: Stelle sicher, dass *kein* Firestore-Aufruf stattfindet.
        // Das beweist, dass der Code sofort aus der Funktion zurückkehrt.
        verify(exactly = 0) { collectionReferenceMock.document(any()) }
        verify(exactly = 0) { documentReferenceMock.get() }
    }

    @Test
    fun `getAll() should return list of testEntities`() = runTest {
        val entity1 = TestEntity(id = "id1", name = "Max Mustermann", active = true)
        val entity2 = TestEntity(id = "id2", name = "Frau Mustermann", active = true)
        val querySnapshotResultMock: QuerySnapshot = mockk()

        mockTaskResult(querySnapshotTaskMock, querySnapshotResultMock)
        // every { querySnapshotResultMock.documents } returns documents // toObjects ist meist einfacher
        every { querySnapshotResultMock.toObjects(TestEntity::class.java) } returns listOf(entity1, entity2)
        // Sicherstellen, dass get() auf der Collection Mock das querySnapshotTaskMock zurückgibt
        every { collectionReferenceMock.get() } returns querySnapshotTaskMock

        val result = testFirestoreRepository.getAll()

        assertEquals(2, result.size)
        assertTrue(result.containsAll(listOf(entity1, entity2)))
        verify { collectionReferenceMock.get() }
    }

    /**
     * ### Erklärung der Schritte:
     *
     * 1.  **Arrange (Vorbereiten):**
     *     *   Wir erstellen eine `simulatedException`. Diese wird die "Ursache" (`cause`) für den Fehler sein,
     *     *     den dein Repository werfen soll.
     *     *   Wir rufen `mockTaskResult(querySnapshotTaskMock, null, simulatedException)` auf. Dies ist der wichtigste
     *     *     Teil. Du sagst MockK, dass der `Task`, der von `.get()` zurückgegeben wird, als "nicht erfolgreich"
     *     *     gilt und unsere `simulatedException` als Fehler hat. Dadurch wird der `await()`-Aufruf in deiner `
     *     *     getAll()`-Methode scheitern und eine Exception werfen.
     *
     * 2.  **Act & Assert (Ausführen & Überprüfen):**
     *     *   Wir verwenden `assertThrows<RuntimeException> { ... }`, um die `getAll()`-Methode aufzurufen. Diese
     *     *     Funktion fängt die erwartete `RuntimeException` für uns ab.
     *     *   Wir überprüfen, ob die `message` der gefangenen Exception dem Text entspricht, den du in
     *     *     `BaseFirestoreRepository.kt` definiert hast.
     *     *   Wir stellen mit `assertEquals(simulatedException, exception.cause)` sicher, dass die *Ursache* der
     *     *     `RuntimeException` genau die Exception ist, die wir im `Arrange`-Teil simuliert haben. Dies ist ein
     *     *     sehr wichtiger Test, um die korrekte Fehlerweitergabe zu validieren.
     *
     * 3.  **Verify (Verifizieren):**
     *     *   Wir prüfen am Ende, ob die Methode `collectionReferenceMock.get()` auch wirklich aufgerufen wurde.
     *
     */
    @Test
    fun `getAll() should throw runtime exception on firestore failure`() = runTest {
        // Arrange: Bereite den Fehlerfall vor
        val simulatedException = RuntimeException("Simulated Firestore error")

        // Stelle sicher, dass der get()-Aufruf den Task zurückgibt, den wir manipulieren wollen.
        // Diese Zeile ist technisch durch setUp() abgedeckt, macht den Test aber lesbarer.
        every { collectionReferenceMock.get() } returns querySnapshotTaskMock

        // Weise den Task an, mit unserer simulierten Exception fehlzuschlagen.
        // Das ist der entscheidende Schritt, um den catch-Block auszulösen.
        mockTaskResult(querySnapshotTaskMock, null, simulatedException)

        // Act & Assert: Führe die Methode aus und fange die erwartete Exception.
        val exception = assertThrows<RuntimeException> {
            testFirestoreRepository.getAll()
        }

        // Überprüfe die Details der gefangenen Exception
        val expectedMessage = "Failed to get all entities from test-collection"
        assertTrue(
            exception.message?.contains(expectedMessage) ?: false,
            "Exception message should contain '$expectedMessage'"
        )
        assertEquals(
            simulatedException,
            exception.cause,
            "The cause of the thrown exception should be the simulated Firestore exception."
        )

        // Verify: Stelle sicher, dass die `get` Methode auf dem Mock aufgerufen wurde.
        verify(exactly = 1) { collectionReferenceMock.get() }
    }

    @Test
    fun `delete should call delete on document`() = runTest {
        val entityId = "entityToDelete"
        mockTaskResult(voidTaskMock, null)
        every { collectionReferenceMock.document(entityId) } returns documentReferenceMock
        every { documentReferenceMock.delete() } returns voidTaskMock

        testFirestoreRepository.delete(entityId)

        verify { collectionReferenceMock.document(entityId) }
        verify { documentReferenceMock.delete() }
    }

    @Test
    fun `delete should throw runtime exception on firestore failure`() = runTest {
        // Arrange
        val entityIdToDelete = "entityToDelete" // Die ID, die wir zu löschen versuchen.
        val simulatedException = RuntimeException("Simulated Firestore error")

        // Mocke die Kette für einen fehlschlagenden delete-Aufruf
        every { collectionReferenceMock.document(entityIdToDelete) } returns documentReferenceMock
        every { documentReferenceMock.delete() } returns voidTaskMock
        mockTaskResult(voidTaskMock, null, simulatedException) // Simuliere, dass der Task fehlschlägt

        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            // Rufe die 'delete'-Methode mit der korrekten ID auf
            testFirestoreRepository.delete(entityIdToDelete)
        }

        val expectedMessage = "Failed to delete entity '$entityIdToDelete' from test-collection"
        assertTrue(exception.message?.contains(expectedMessage) ?: false)
        assertEquals(
            simulatedException,
            exception.cause,
            "Die Ursache der Exception (cause) ist nicht die simulierte Exception."
        )

        verify(exactly = 1) { collectionReferenceMock.document(entityIdToDelete) }
        verify(exactly = 1) { documentReferenceMock.delete() }
    }

    @Test
    fun `delete with blank id should throw IllegalArgumentException`() = runTest {
        // Arrange
        val blankId = "   " // Eine ID, die nur aus Leerzeichen besteht.

        // Act & Assert
        // Erwarte eine IllegalArgumentException, wenn die delete-Methode aufgerufen wird.
        val exception = assertThrows<IllegalArgumentException> {
            testFirestoreRepository.delete(blankId)
        }

        // Überprüfe die Nachricht der geworfenen Exception.
        assertEquals("Document ID cannot be blank for deletion.", exception.message)

        // Verify
        // Stelle sicher, dass keine Firestore-Operationen (document() oder delete())
        // aufgerufen wurden, da der Code vorher abbrechen sollte.
        verify(exactly = 0) { collectionReferenceMock.document(any()) }
        verify(exactly = 0) { documentReferenceMock.delete() }
    }
}
