package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.data.Event
import de.geosphere.speechplaning.data.model.CongregationEvent
import de.geosphere.speechplaning.data.services.FirestoreService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class CongregationEventRepositoryTest {

    private lateinit var firestoreService: FirestoreService
    private lateinit var repository: CongregationEventRepository

    private val testDistrictId = "testDistrictId001"
    private val testCongregationId = "testCongId002"
    private val testEventId = "testEventId003"
    private lateinit var testEvent: CongregationEvent
    private lateinit var newEventWithoutId: CongregationEvent

    private val districtsCollectionName = "districts"
    private val congregationsSubcollectionName = "congregations"
    private val congregationEventsSubcollectionName = "congregationEvents"

    private val expectedParentCollectionPathForEvent =
        "$districtsCollectionName/$testDistrictId/$congregationsSubcollectionName"
    private val expectedParentDocumentIdForEvent = testCongregationId

    @BeforeEach
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        repository = CongregationEventRepository(firestoreService)

        testEvent = CongregationEvent(
            id = testEventId,
            congregationId = testCongregationId,
            date = LocalDate.now(),
            eventType = Event.MEMORIAL,
            speechId = "speechXYZ",
            speakerId = "speakerABC",
            chairmanId = "chairman123",
            notes = "Wichtige Notizen für das Test-Event"
        )

        newEventWithoutId = CongregationEvent(
            id = "",
            congregationId = testCongregationId,
            date = LocalDate.now().plusDays(7),
            eventType = Event.MEMORIAL,
            notes = "Notizen für ein brandneues Event"
        )
    }

    @Nested
    inner class SaveEvent {
        @Test
        fun `saveEvent for new event should call firestoreService add and return new id`() = runTest {
            val generatedId = "newGeneratedEventId"
            coEvery {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = expectedParentCollectionPathForEvent,
                    parentId = expectedParentDocumentIdForEvent,
                    subcollection = congregationEventsSubcollectionName,
                    data = newEventWithoutId
                )
            } returns generatedId

            val resultId = repository.saveEvent(testDistrictId, testCongregationId, newEventWithoutId)

            assertEquals(generatedId, resultId)
            coVerify {
                firestoreService.addDocumentToSubcollection(
                    expectedParentCollectionPathForEvent,
                    expectedParentDocumentIdForEvent,
                    congregationEventsSubcollectionName,
                    newEventWithoutId
                )
            }
        }

        @Test
        fun `saveEvent for existing event should call firestoreService set and return existing id`() = runTest {
            coEvery {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = expectedParentCollectionPathForEvent,
                    parentId = expectedParentDocumentIdForEvent,
                    subcollection = congregationEventsSubcollectionName,
                    documentId = testEvent.id,
                    data = testEvent
                )
            } returns Unit // setDocumentInSubcollection gibt Unit zurück

            val resultId = repository.saveEvent(testDistrictId, testCongregationId, testEvent)

            assertEquals(testEvent.id, resultId)
            coVerify {
                firestoreService.setDocumentInSubcollection(
                    expectedParentCollectionPathForEvent,
                    expectedParentDocumentIdForEvent,
                    congregationEventsSubcollectionName,
                    testEvent.id,
                    testEvent
                )
            }
        }

        @Test
        fun `saveEvent for new event should throw exception if firestoreService add fails`() = runTest {
            val errorMessage = "Firestore add operation failed"
            coEvery {
                firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                repository.saveEvent(testDistrictId, testCongregationId, newEventWithoutId)
            }
            assertEquals(
                "Failed to save entity '[new]' in subcollection '$congregationEventsSubcollectionName' under " +
                    "parent '${expectedParentDocumentIdForEvent}' in '$expectedParentCollectionPathForEvent'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }

        @Test
        fun `saveEvent for existing event should throw exception if firestoreService set fails`() = runTest {
            val errorMessage = "Firestore set operation failed"
            coEvery {
                firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                repository.saveEvent(testDistrictId, testCongregationId, testEvent)
            }
            assertEquals(
                "Failed to save entity '${testEvent.id}' in subcollection '$congregationEventsSubcollectionName' " +
                    "under parent '${expectedParentDocumentIdForEvent}' in '$expectedParentCollectionPathForEvent'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class GetEventById {
        @Test
        fun `getEventById should return event when event is found`() = runTest {
            coEvery {
                firestoreService.getDocumentFromSubcollection(
                    parentCollectionPath = expectedParentCollectionPathForEvent,
                    parentDocumentId = expectedParentDocumentIdForEvent,
                    subcollectionName = congregationEventsSubcollectionName,
                    documentId = testEventId,
                    objectClass = CongregationEvent::class.java
                )
            } returns testEvent

            val result = repository.getEventById(testDistrictId, testCongregationId, testEventId)

            assertEquals(testEvent, result)
            coVerify {
                firestoreService.getDocumentFromSubcollection(
                    expectedParentCollectionPathForEvent,
                    expectedParentDocumentIdForEvent,
                    congregationEventsSubcollectionName,
                    testEventId,
                    CongregationEvent::class.java
                )
            }
        }

        @Test
        fun `getEventById should return null when eventId is blank`() = runTest {
            val result = repository.getEventById(testDistrictId, testCongregationId, "")
            assertNull(result)
            coVerify(exactly = 0) {
                firestoreService.getDocumentFromSubcollection(
                    any(),
                    any(),
                    any(),
                    any(),
                    eq(CongregationEvent::class.java)
                )
            }
        }

        @Test
        fun `getEventById should return null when firestoreService returns null`() = runTest {
            coEvery {
                firestoreService.getDocumentFromSubcollection(
                    parentCollectionPath = expectedParentCollectionPathForEvent,
                    parentDocumentId = expectedParentDocumentIdForEvent,
                    subcollectionName = congregationEventsSubcollectionName,
                    documentId = testEventId,
                    objectClass = CongregationEvent::class.java
                )
            } returns null

            val result = repository.getEventById(testDistrictId, testCongregationId, testEventId)

            assertNull(result)
        }

        @Test
        fun `getEventById should throw exception when firestoreService fails`() = runTest {
            val errorMessage = "Firestore get operation failed"
            coEvery {
                firestoreService.getDocumentFromSubcollection(
                    any(),
                    any(),
                    any(),
                    eq(testEventId),
                    eq(CongregationEvent::class.java)
                )
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                repository.getEventById(testDistrictId, testCongregationId, testEventId)
            }
            assertEquals(
                "Failed to get entity '$testEventId' from subcollection '$congregationEventsSubcollectionName' " +
                    "under parent '${expectedParentDocumentIdForEvent}' in '$expectedParentCollectionPathForEvent'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class GetAllEventsForCongregation {
        @Test
        fun `getAllEventsForCongregation should call firestoreService and return events list`() = runTest {
            val expectedEvents = listOf(testEvent, newEventWithoutId.copy(id = "anotherEventId"))
            coEvery {
                firestoreService.getDocumentsFromSubcollection(
                    parentCollection = expectedParentCollectionPathForEvent,
                    parentId = expectedParentDocumentIdForEvent,
                    subcollection = congregationEventsSubcollectionName,
                    objectClass = CongregationEvent::class.java
                )
            } returns expectedEvents

            val result = repository.getAllEventsForCongregation(testDistrictId, testCongregationId)

            assertEquals(expectedEvents, result)
            coVerify {
                firestoreService.getDocumentsFromSubcollection(
                    expectedParentCollectionPathForEvent,
                    expectedParentDocumentIdForEvent,
                    congregationEventsSubcollectionName,
                    CongregationEvent::class.java
                )
            }
        }

        @Test
        fun `getAllEventsForCongregation should return empty list if firestoreService returns empty list`() = runTest {
            coEvery {
                firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(CongregationEvent::class.java))
            } returns emptyList()

            val result = repository.getAllEventsForCongregation(testDistrictId, testCongregationId)

            assertEquals(0, result.size)
        }

        @Test
        fun `getAllEventsForCongregation should throw exception if firestoreService fails`() = runTest {
            val errorMessage = "Firestore get all operation failed"
            coEvery {
                firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(CongregationEvent::class.java))
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                repository.getAllEventsForCongregation(testDistrictId, testCongregationId)
            }
            assertEquals(
                "Failed to get all entities from subcollection '$congregationEventsSubcollectionName' under " +
                    "parent '${expectedParentDocumentIdForEvent}' in '$expectedParentCollectionPathForEvent'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class DeleteEvent {
        @Test
        fun `deleteEvent should call firestoreService with correct paths`() = runTest {
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(
                    parentCollection = expectedParentCollectionPathForEvent,
                    parentId = expectedParentDocumentIdForEvent,
                    subcollection = congregationEventsSubcollectionName,
                    documentId = testEventId
                )
            } returns Unit

            repository.deleteEvent(testDistrictId, testCongregationId, testEventId)

            coVerify {
                firestoreService.deleteDocumentFromSubcollection(
                    expectedParentCollectionPathForEvent,
                    expectedParentDocumentIdForEvent,
                    congregationEventsSubcollectionName,
                    testEventId
                )
            }
        }

        @Test
        fun `deleteEvent should throw exception if firestoreService fails`() = runTest {
            val errorMessage = "Firestore delete operation failed"
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                repository.deleteEvent(testDistrictId, testCongregationId, testEventId)
            }
            assertEquals(
                "Failed to delete entity '$testEventId' from subcollection '$congregationEventsSubcollectionName' " +
                    "under parent '${expectedParentDocumentIdForEvent}' in '$expectedParentCollectionPathForEvent'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }

        @Test
        fun `deleteEvent should throw IllegalArgumentException when eventId is blank`() {
            val exception = assertThrows<IllegalArgumentException> {
                runTest { repository.deleteEvent(testDistrictId, testCongregationId, "") }
            }
            assertEquals("Document ID cannot be blank for deletion.", exception.message)
            coVerify(exactly = 0) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
        }
    }

    @Nested
    inner class HelperMethodTests {
        @Test
        fun `extractIdFromEntity should return entity id`() {
            assertEquals(testEvent.id, repository.extractIdFromEntity(testEvent))
            assertEquals(newEventWithoutId.id, repository.extractIdFromEntity(newEventWithoutId))
        }

        @Test
        fun `buildParentCollectionPath returns correct path with districtId and congregationId`() {
            val path = repository.buildParentCollectionPath(testDistrictId, testCongregationId)
            assertEquals(expectedParentCollectionPathForEvent, path) // Verwendet die oben definierte Konstante
        }

        @Test
        fun `buildParentCollectionPath throws exception when parentIds count is not two`() {
            assertThrows<IllegalArgumentException>("Expected districtId and congregationId as parentIds") {
                repository.buildParentCollectionPath(testDistrictId) // Nur eine ID
            }
            assertThrows<IllegalArgumentException>("Expected districtId and congregationId as parentIds") {
                repository.buildParentCollectionPath() // Keine IDs
            }
            assertThrows<IllegalArgumentException>("Expected districtId and congregationId as parentIds") {
                repository.buildParentCollectionPath(testDistrictId, testCongregationId, "extraId") // Drei IDs
            }
        }

        @Test
        fun `getParentDocumentId returns congregationId when two parentIds are provided`() {
            val result = repository.getParentDocumentId(testDistrictId, testCongregationId)
            assertEquals(testCongregationId, result) // testCongregationId ist expectedParentDocumentIdForEvent
        }

        @Test
        fun `getParentDocumentId throws exception when parentIds count is not two`() {
            assertThrows<IllegalArgumentException>("Expected districtId and congregationId as parentIds") {
                repository.getParentDocumentId(testDistrictId) // Nur eine ID
            }
            assertThrows<IllegalArgumentException>("Expected districtId and congregationId as parentIds") {
                repository.getParentDocumentId() // Keine IDs
            }
            assertThrows<IllegalArgumentException>("Expected districtId and congregationId as parentIds") {
                repository.getParentDocumentId(testDistrictId, testCongregationId, "extraId") // Drei IDs
            }
        }
    }
}
