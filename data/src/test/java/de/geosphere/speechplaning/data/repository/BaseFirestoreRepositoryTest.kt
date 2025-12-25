package de.geosphere.speechplaning.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import de.geosphere.speechplaning.data.repository.base.FirestoreRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

internal data class TestEntity(val id: String = "", val name: String = "")

@OptIn(ExperimentalCoroutinesApi::class)
internal class TestIFirestoreRepositoryImpl(firestore: FirebaseFirestore) : FirestoreRepository<TestEntity>(
    firestore = firestore,
    collectionPath = "test-collection",
    clazz = TestEntity::class.java
) {
    override fun extractIdFromEntity(entity: TestEntity): String = entity.id
}

@ExperimentalCoroutinesApi
class BaseFirestoreRepositoryTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    lateinit var firestore: FirebaseFirestore
    lateinit var collectionReference: CollectionReference
    lateinit var documentReference: DocumentReference
    lateinit var querySnapshot: QuerySnapshot
    lateinit var documentSnapshot: DocumentSnapshot

    lateinit var repository: TestIFirestoreRepositoryImpl

    beforeSpec {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    afterSpec {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    beforeEach {
        Dispatchers.setMain(testDispatcher)

        firestore = mockk()
        collectionReference = mockk()
        documentReference = mockk()
        querySnapshot = mockk()
        documentSnapshot = mockk()

        every { firestore.collection("test-collection") } returns collectionReference

        repository = TestIFirestoreRepositoryImpl(firestore)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    given("save") {
        `when`("saving a new entity (with blank ID)") {
            then("it should call 'add' and return the new ID") {
                val newEntity = TestEntity(id = "", name = "New")
                val generatedId = "firestore-generated-id"

                val addTask = mockk<Task<DocumentReference>>()
                coEvery { addTask.await() } returns documentReference
                every { collectionReference.add(newEntity) } returns addTask
                every { documentReference.id } returns generatedId

                val resultId = repository.save(newEntity)
                testScope.advanceUntilIdle()

                resultId shouldBe generatedId
                verify { collectionReference.add(newEntity) }
                verify(exactly = 0) { collectionReference.document(any()) }
            }
        }

        `when`("saving an existing entity (with given ID)") {
            then("it should call 'set' and return the existing ID") {
                val existingEntity = TestEntity(id = "existing-id", name = "Existing")

                val setTask = mockk<Task<Void>>()
                coEvery { setTask.await() } returns mockk()
                every { collectionReference.document(existingEntity.id) } returns documentReference
                every { documentReference.set(existingEntity) } returns setTask

                val resultId = repository.save(existingEntity)
                testScope.advanceUntilIdle()

                resultId shouldBe existingEntity.id
                verify { documentReference.set(existingEntity) }
                verify(exactly = 0) { collectionReference.add(any()) }
            }
        }

        `when`("Firestore fails during save of an existing entity") {
            then("it should throw a RuntimeException with the entity ID in the message") {
                val entity = TestEntity(id = "some-id", name = "Failure")
                val exception = Exception("Firestore is down")

                val setTask = mockk<Task<Void>>()
                coEvery { setTask.await() } throws exception
                every { collectionReference.document(entity.id) } returns documentReference
                every { documentReference.set(entity) } returns setTask

                val thrown = shouldThrow<RuntimeException> {
                    repository.save(entity)
                }
                testScope.advanceUntilIdle()
                thrown.message shouldBe "Failed to save entity 'some-id' in test-collection"
                thrown.cause shouldBe exception
            }
        }

        `when`("Firestore fails during save of a new entity") {
            then("it should throw a RuntimeException with '[new]' in the message") {
                val newEntity = TestEntity(id = "", name = "New Failure")
                val exception = Exception("Firestore is down")

                val addTask = mockk<Task<DocumentReference>>()
                coEvery { addTask.await() } throws exception
                every { collectionReference.add(newEntity) } returns addTask

                val thrown = shouldThrow<RuntimeException> {
                    repository.save(newEntity)
                }
                testScope.advanceUntilIdle()
                thrown.message shouldBe "Failed to save entity '[new]' in test-collection"
                thrown.cause shouldBe exception
            }
        }
    }

    given("getById") {
        `when`("the document exists") {
            then("it should return the entity") {
                val entityId = "existing-id"
                val expectedEntity = TestEntity(id = entityId, name = "Found")

                val getTask = mockk<Task<DocumentSnapshot>>()
                coEvery { getTask.await() } returns documentSnapshot
                every { documentSnapshot.toObject(TestEntity::class.java) } returns expectedEntity
                every { collectionReference.document(entityId) } returns documentReference
                every { documentReference.get() } returns getTask

                val result = repository.getById(entityId)
                testScope.advanceUntilIdle()
                result shouldBe expectedEntity
            }
        }

        `when`("the document does not exist") {
            then("it should return null") {
                val entityId = "non-existing-id"

                val getTask = mockk<Task<DocumentSnapshot>>()
                coEvery { getTask.await() } returns documentSnapshot
                every { documentSnapshot.toObject(TestEntity::class.java) } returns null
                every { collectionReference.document(entityId) } returns documentReference
                every { documentReference.get() } returns getTask

                val result = repository.getById(entityId)
                testScope.advanceUntilIdle()
                result shouldBe null
            }
        }

        `when`("the ID is blank") {
            then("it should return null without calling Firestore") {
                repository.getById(" ")
                testScope.advanceUntilIdle()
                verify(exactly = 0) { collectionReference.document(any()) }
            }
        }
    }

    given("getAll") {
        `when`("the collection has documents") {
            then("it should return a list of entities") {
                val expectedList = listOf(TestEntity("id1", "One"), TestEntity("id2", "Two"))

                val getTask = mockk<Task<QuerySnapshot>>()
                coEvery { getTask.await() } returns querySnapshot
                every { querySnapshot.toObjects(TestEntity::class.java) } returns expectedList
                every { collectionReference.get() } returns getTask

                val result = repository.getAll()
                testScope.advanceUntilIdle()
                result shouldBe expectedList
            }
        }
    }

    given("delete") {
        `when`("deleting with a valid ID") {
            then("it should call delete on the document") {
                val entityId = "id-to-delete"

                val deleteTask = mockk<Task<Void>>()
                coEvery { deleteTask.await() } returns mockk()
                every { collectionReference.document(entityId) } returns documentReference
                every { documentReference.delete() } returns deleteTask

                repository.delete(entityId)
                testScope.advanceUntilIdle()
                verify { documentReference.delete() }
            }
        }

        `when`("deleting with a blank ID") {
            then("it should throw an IllegalArgumentException") {
                shouldThrow<IllegalArgumentException> {
                    repository.delete("   ")
                }
            }
        }
    }
})
