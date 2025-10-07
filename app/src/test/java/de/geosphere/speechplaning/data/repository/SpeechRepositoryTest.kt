package de.geosphere.speechplaning.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import de.geosphere.speechplaning.data.model.Speech
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class SpeechRepositoryTest : BehaviorSpec({

    val firestoreMock: FirebaseFirestore = mockk()
    val collectionReferenceMock: CollectionReference = mockk()
    val documentReferenceMock: DocumentReference = mockk()
    val queryMock: Query = mockk()

    // Tasks Mocks - Using relaxed = true to avoid mocking all Task methods
    val voidTaskMock: Task<Void> = mockk(relaxed = true)
    val documentReferenceTaskMock: Task<DocumentReference> = mockk(relaxed = true)
    val querySnapshotTaskMock: Task<QuerySnapshot> = mockk(relaxed = true)
    val documentSnapshotTaskMock: Task<DocumentSnapshot> = mockk(relaxed = true)

    lateinit var speechRepository: SpeechRepository

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

        speechRepository = SpeechRepository(firestoreMock)
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

    init {
        "extractIdFromEntity should return correct id" {
            val speech = Speech(id = "testId", subject = "Active Speech", active = true)
            val extractedId = speechRepository.extractIdFromEntity(speech)
            extractedId shouldBe "testId"
        }

        given("getActiveSpeeches") {
            `when`("query is successful") {
                then("it should query and return active speeches") {
                    val activeSpeech = Speech(id = "active1", subject = "Active Speech", active = true)
                    val querySnapshotResultMock: QuerySnapshot = mockk()

                    mockTaskResult(querySnapshotTaskMock, querySnapshotResultMock)
                    every { querySnapshotResultMock.toObjects(Speech::class.java) } returns listOf(activeSpeech)

                    every { collectionReferenceMock.whereEqualTo("active", true) } returns queryMock
                    every { queryMock.get() } returns querySnapshotTaskMock

                    val result = speechRepository.getActiveSpeeches()

                    result.size shouldBe 1
                    result[0] shouldBe activeSpeech
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
                        speechRepository.getActiveSpeeches()
                    }

                    exception.message shouldContain "Failed to get active speech from speeches"
                    exception.cause shouldBe simulatedException
                    verify { collectionReferenceMock.whereEqualTo("active", true) }
                    verify { queryMock.get() }
                }
            }
        }
    }
})
