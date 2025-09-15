package de.geosphere.speechplaning.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import de.geosphere.speechplaning.data.model.District
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@ExperimentalCoroutinesApi
class DistrictRepositoryTest {

    @MockK
    private lateinit var firestoreMock: FirebaseFirestore

    @MockK
    private lateinit var collectionReferenceMock: CollectionReference

    @MockK
    private lateinit var queryMock: Query

    @MockK(relaxed = true)
    private lateinit var querySnapshotTaskMock: Task<QuerySnapshot>

    private lateinit var districtRepository: DistrictRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this) // Initialize @MockK annotated mocks

        every { firestoreMock.collection(any()) } returns collectionReferenceMock
        every { collectionReferenceMock.whereEqualTo(any<String>(), any()) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock

        districtRepository = DistrictRepository(firestoreMock)
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
        val district = District(id = "testId", name = "Test District")
        val extractedId = districtRepository.extractIdFromEntity(district)
        Assertions.assertEquals("testId", extractedId)
    }

    @Test
    fun `getActiveDistricts should query and return active districts`() = runTest {
        val activeDistrict = District(id = "active1", circuitOverseerId = "1", name = "Max Mustermann", active = true)
        val querySnapshotResultMock: QuerySnapshot = mockk()

        mockTaskResult(querySnapshotTaskMock, querySnapshotResultMock)
        every { querySnapshotResultMock.toObjects(District::class.java) } returns listOf(activeDistrict)

        every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock

        val result = districtRepository.getActiveDistricts()

        assertEquals(1, result.size)
        assertEquals(activeDistrict, result[0])
        verify { collectionReferenceMock.whereEqualTo("active", true) }
        verify { queryMock.get() }
    }

    @Test
    fun `getActiveDistricts should throw runtime exception on firestore failure`() = runTest {
        val simulatedException = RuntimeException("Simulated Firestore error")

        // Mock the query chain to return a failing task
        every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock
        mockTaskResult(querySnapshotTaskMock, null, simulatedException)

        val exception = assertThrows<RuntimeException> {
            districtRepository.getActiveDistricts()
        }

        assertTrue(exception.message?.contains("Failed to get active district from districts") ?: false)
        assertEquals(simulatedException, exception.cause)
        verify { collectionReferenceMock.whereEqualTo("active", true) }
        verify { queryMock.get() }
    }
}
