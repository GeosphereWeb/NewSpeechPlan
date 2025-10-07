package de.geosphere.speechplaning.data.repository.base

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

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

class BaseFirestoreRepositoryTest : BehaviorSpec({

    val firestoreMock: FirebaseFirestore = mockk()
    val collectionReferenceMock: CollectionReference = mockk()
    val documentReferenceMock: DocumentReference = mockk()
    val queryMock: Query = mockk()

    // Tasks Mocks - Using relaxed = true to avoid mocking all Task methods
    val voidTaskMock: Task<Void> = mockk(relaxed = true)
    val documentReferenceTaskMock: Task<DocumentReference> = mockk(relaxed = true)
    val querySnapshotTaskMock: Task<QuerySnapshot> = mockk(relaxed = true)
    val documentSnapshotTaskMock: Task<DocumentSnapshot> = mockk(relaxed = true)
    lateinit var testFirestoreRepository: TestFirestoreRepository

    beforeEach {
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

    init {
        "extractIdFromEntity should return correct id" {
            val district = TestEntity(id = "testId", name = "Test District")
            val extractedId = testFirestoreRepository.extractIdFromEntity(district)
            extractedId shouldBe "testId"
        }

        given("saving an entity") {
            `when`("entity is new") {
                then("it should add to firestore and return new id") {
                    val newTestEntity = TestEntity(id = "", name = "Max Mustermann", active = true)
                    val generatedId = "generatedFirebaseId"
                    val addedDocRefMock: DocumentReference = mockk() // separater Mock für das Ergebnis von add()

                    mockTaskResult(documentReferenceTaskMock, addedDocRefMock)
                    every { addedDocRefMock.id } returns generatedId

                    val resultId = testFirestoreRepository.save(newTestEntity)

                    verify { collectionReferenceMock.add(newTestEntity) }
                    resultId shouldBe generatedId
                }

                then("it should throw runtime exception on firestore failure") {
                    val newEntity = TestEntity(id = "", name = "Max Mustermann", active = true)
                    val simpleException = RuntimeException("Simulated Firestore error")

                    mockTaskResult(documentReferenceTaskMock, null, simpleException)
                    every { collectionReferenceMock.add(newEntity) } returns documentReferenceTaskMock

                    val exception = shouldThrow<RuntimeException> {
                        testFirestoreRepository.save(newEntity)
                    }

                    exception.message shouldContain "Failed to save entity '[new]' in test-collection"
                    exception.cause shouldBe simpleException
                }
            }

            `when`("entity exists") {
                then("it should set document in firestore and return existing id") {
                    val existingDistrict = TestEntity(
                        id = "existingId",
                        name = "Max Mustermann",
                        active = true
                    )
                    mockTaskResult(voidTaskMock, null)

                    val resultId = testFirestoreRepository.save(existingDistrict)

                    verify { collectionReferenceMock.document("existingId") }
                    verify { documentReferenceMock.set(existingDistrict) }
                    resultId shouldBe "existingId"
                }

                then("it should throw runtime exception on firestore failure") {
                    val existingEntity = TestEntity(id = "existingId", name = "Existing", active = true)
                    val simulatedException = RuntimeException("Simulated Firestore set failure")

                    every { collectionReferenceMock.document(existingEntity.id) } returns documentReferenceMock
                    every { documentReferenceMock.set(existingEntity) } returns voidTaskMock
                    mockTaskResult(voidTaskMock, null, simulatedException)

                    val exception = shouldThrow<RuntimeException> {
                        testFirestoreRepository.save(existingEntity)
                    }

                    exception.message shouldContain "Failed to save entity '${existingEntity.id}' in test-collection"
                    exception.cause shouldBe simulatedException

                    verify(exactly = 1) { collectionReferenceMock.document(existingEntity.id) }
                    verify(exactly = 1) { documentReferenceMock.set(existingEntity) }
                }
            }
        }

        given("getting an entity by id") {
            `when`("id is valid and exists") {
                then("it should return testEntity object") {
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
                    every { collectionReferenceMock.document(testEntityId) } returns documentReferenceMock
                    every { documentReferenceMock.get() } returns documentSnapshotTaskMock

                    val result = testFirestoreRepository.getById(testEntityId)

                    result shouldBe expectedDistrict
                    verify { collectionReferenceMock.document(testEntityId) }
                }
            }

            `when`("id is invalid") {
                then("it should return null") {
                    val testEntityId = "nonExistingId"
                    val snapshotResultMock: DocumentSnapshot = mockk()

                    mockTaskResult(documentSnapshotTaskMock, snapshotResultMock)
                    every { snapshotResultMock.exists() } returns false
                    every { snapshotResultMock.toObject(TestEntity::class.java) } returns null
                    every { collectionReferenceMock.document(testEntityId) } returns documentReferenceMock
                    every { documentReferenceMock.get() } returns documentSnapshotTaskMock

                    val result = testFirestoreRepository.getById(testEntityId)

                    result.shouldBeNull()
                    verify { collectionReferenceMock.document(testEntityId) }
                }
            }

            `when`("id is blank") {
                then("it should return null immediately") {
                    val blankId = "   "
                    val result = testFirestoreRepository.getById(blankId)
                    result.shouldBeNull()
                    verify(exactly = 0) { collectionReferenceMock.document(any()) }
                    verify(exactly = 0) { documentReferenceMock.get() }
                }
            }

            `when`("firestore fails") {
                then("it should throw a runtime exception") {
                    val entityId = "entity123"
                    val simulatedException = RuntimeException("Simulated Firestore error")

                    every { collectionReferenceMock.document(entityId) } returns documentReferenceMock
                    every { documentReferenceMock.get() } returns documentSnapshotTaskMock
                    mockTaskResult(documentSnapshotTaskMock, null, simulatedException)

                    val exception = shouldThrow<RuntimeException> {
                        testFirestoreRepository.getById(entityId)
                    }

                    exception.message shouldContain "Failed to get entity '$entityId' from test-collection"
                    exception.cause shouldBe simulatedException

                    verify(exactly = 1) { collectionReferenceMock.document(entityId) }
                    verify(exactly = 1) { documentReferenceMock.get() }
                }
            }
        }

        given("getting all entities") {
            `when`("entities exist") {
                then("it should return list of testEntities") {
                    val entity1 = TestEntity(id = "id1", name = "Max Mustermann", active = true)
                    val entity2 = TestEntity(id = "id2", name = "Frau Mustermann", active = true)
                    val querySnapshotResultMock: QuerySnapshot = mockk()

                    mockTaskResult(querySnapshotTaskMock, querySnapshotResultMock)
                    every { querySnapshotResultMock.toObjects(TestEntity::class.java) } returns listOf(entity1, entity2)
                    every { collectionReferenceMock.get() } returns querySnapshotTaskMock

                    val result = testFirestoreRepository.getAll()

                    result.size shouldBe 2
                    result shouldContainAll listOf(entity1, entity2)
                    verify { collectionReferenceMock.get() }
                }
            }

            `when`("firestore fails") {
                then("it should throw runtime exception") {
                    val simulatedException = RuntimeException("Simulated Firestore error")

                    every { collectionReferenceMock.get() } returns querySnapshotTaskMock
                    mockTaskResult(querySnapshotTaskMock, null, simulatedException)

                    val exception = shouldThrow<RuntimeException> {
                        testFirestoreRepository.getAll()
                    }

                    exception.message shouldContain "Failed to get all entities from test-collection"
                    exception.cause shouldBe simulatedException

                    verify(exactly = 1) { collectionReferenceMock.get() }
                }
            }
        }

        given("deleting an entity") {
            `when`("id is valid") {
                then("it should call delete on document") {
                    val entityId = "entityToDelete"
                    mockTaskResult(voidTaskMock, null)
                    every { collectionReferenceMock.document(entityId) } returns documentReferenceMock
                    every { documentReferenceMock.delete() } returns voidTaskMock

                    testFirestoreRepository.delete(entityId)

                    verify { collectionReferenceMock.document(entityId) }
                    verify { documentReferenceMock.delete() }
                }
            }

            `when`("id is blank") {
                then("it should throw IllegalArgumentException") {
                    val blankId = "   "
                    val exception = shouldThrow<IllegalArgumentException> {
                        testFirestoreRepository.delete(blankId)
                    }

                    exception.message shouldBe "Document ID cannot be blank for deletion."

                    verify(exactly = 0) { collectionReferenceMock.document(any()) }
                    verify(exactly = 0) { documentReferenceMock.delete() }
                }
            }

            `when`("firestore fails") {
                then("it should throw runtime exception") {
                    val entityIdToDelete = "entityToDelete"
                    val simulatedException = RuntimeException("Simulated Firestore error")

                    every { collectionReferenceMock.document(entityIdToDelete) } returns documentReferenceMock
                    every { documentReferenceMock.delete() } returns voidTaskMock
                    mockTaskResult(voidTaskMock, null, simulatedException)

                    val exception = shouldThrow<RuntimeException> {
                        testFirestoreRepository.delete(entityIdToDelete)
                    }

                    exception.message shouldContain "Failed to delete entity '$entityIdToDelete' from test-collection"
                    exception.cause shouldBe simulatedException

                    verify(exactly = 1) { collectionReferenceMock.document(entityIdToDelete) }
                    verify(exactly = 1) { documentReferenceMock.delete() }
                }
            }
        }
    }
})
