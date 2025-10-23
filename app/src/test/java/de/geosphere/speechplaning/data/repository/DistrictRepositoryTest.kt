package de.geosphere.speechplaning.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import de.geosphere.speechplaning.data.model.District
import de.geosphere.speechplaning.data.model.repository.DistrictRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DistrictRepositoryTest : BehaviorSpec({

    lateinit var firestoreMock: FirebaseFirestore
    lateinit var collectionReferenceMock: CollectionReference
    lateinit var queryMock: Query
    lateinit var querySnapshotTaskMock: Task<QuerySnapshot>

    lateinit var districtRepository: DistrictRepository

    beforeEach {
        firestoreMock = mockk()
        collectionReferenceMock = mockk()
        queryMock = mockk()
        querySnapshotTaskMock = mockk(relaxed = true)

        every { firestoreMock.collection(any()) } returns collectionReferenceMock
        every { collectionReferenceMock.whereEqualTo(any<String>(), any()) } returns queryMock
        every { queryMock.get() } returns querySnapshotTaskMock

        districtRepository = DistrictRepository(firestoreMock)
    }

    // Helper to easily mock Task results for await()
    fun <T> mockTaskResult(task: Task<T>, resultData: T?, exception: Exception? = null) {
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

    given("extractIdFromEntiy") {
        `when`("a District entity is provided") {
            then("it should return the id of that entity") {
                val district = District(id = "123", circuitOverseerId = "456", name = "Test District", active = true)
                val result = districtRepository.extractIdFromEntity(district)
                result shouldBe "123"
            }
        }
    }

    given("getActiveDistricts") {
        `when`("query is successful") {
            then("it should query and return active districts") {
                val activeDistrict = District(
                    id = "active1",
                    circuitOverseerId = "1",
                    name = "Max Mustermann",
                    active = true
                )
                val querySnapshotResultMock: QuerySnapshot = mockk()

                mockTaskResult(querySnapshotTaskMock, querySnapshotResultMock)
                every { querySnapshotResultMock.toObjects(District::class.java) } returns listOf(activeDistrict)

                every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
                every { queryMock.get() } returns querySnapshotTaskMock

                val result = districtRepository.getActiveDistricts()

                result.size shouldBe 1
                result[0] shouldBe activeDistrict
                verify { collectionReferenceMock.whereEqualTo("active", true) }
                verify { queryMock.get() }
            }
        }

        `when`("firestore fails") {
            then("it should throw a runtime exception") {
                val simulatedException = RuntimeException("Simulated Firestore error")

                every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
                every { queryMock.get() } returns querySnapshotTaskMock
                mockTaskResult(querySnapshotTaskMock, null, simulatedException)

                val exception = shouldThrow<RuntimeException> {
                    districtRepository.getActiveDistricts()
                }

                exception.message shouldContain "Failed to get active district from districts"
                exception.cause shouldBe simulatedException
                verify { collectionReferenceMock.whereEqualTo("active", true) }
                verify { queryMock.get() }
            }
        }
    }
})
