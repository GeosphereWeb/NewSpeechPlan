package de.geosphere.speechplaning.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import de.geosphere.speechplaning.data.model.Speech
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class SpeechRepositoryTest {

    private val firestoreMock: FirebaseFirestore = mockk()
    private val collectionReferenceMock: CollectionReference = mockk()
    private val documentReferenceMock: DocumentReference = mockk()
    private val queryMock: Query = mockk()

    // Tasks Mocks - Using relaxed = true to avoid mocking all Task methods
    private val voidTaskMock: Task<Void> = mockk(relaxed = true)
    private val documentReferenceTaskMock: Task<DocumentReference> = mockk(relaxed = true)
    private val querySnapshotTaskMock: Task<QuerySnapshot> = mockk(relaxed = true)
    private val documentSnapshotTaskMock: Task<DocumentSnapshot> = mockk(relaxed = true)

    private lateinit var speechRepository: SpeechRepository

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

        speechRepository = SpeechRepository(firestoreMock)
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
        val speech = Speech(id = "testId", subject = "Active Speech", active = true)
        val extractedId = speechRepository.extractIdFromEntity(speech)
        assertEquals("testId", extractedId)
    }

    @Test
    fun `getActiveSpeeches should query and return active speeches`() = runTest {
        val activeSpeech = Speech(id = "active1", subject = "Active Speech", active = true)
        // val inactiveSpeech = Speech(id = "inactive1", subject = "Inactive Speech", active = false)
        // Nicht benötigt, da Query serverseitig filtert
        val querySnapshotResultMock: QuerySnapshot = mockk()

        mockTaskResult(querySnapshotTaskMock, querySnapshotResultMock)
        every { querySnapshotResultMock.toObjects(Speech::class.java) } returns listOf(activeSpeech)

        every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock

        val result = speechRepository.getActiveSpeeches()

        assertEquals(1, result.size)
        assertEquals(activeSpeech, result[0])
        verify { collectionReferenceMock.whereEqualTo("active", true) }
        verify { queryMock.get() }
    }

    @Test
    fun `getActiveSpeeches should throw runtime exception on firestore failure`() = runTest {
        val simulatedException = RuntimeException("Simulated Firestore error")

        // Mock the query chain to return a failing task
        every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock
        mockTaskResult(querySnapshotTaskMock, null, simulatedException)

        val exception = assertThrows<RuntimeException> {
            speechRepository.getActiveSpeeches()
        }

        Assertions.assertTrue(exception.message?.contains("Failed to get active speech from speeches") ?: false)
        assertEquals(simulatedException, exception.cause)
        verify { collectionReferenceMock.whereEqualTo("active", true) }
        verify { queryMock.get() }
    }
}
