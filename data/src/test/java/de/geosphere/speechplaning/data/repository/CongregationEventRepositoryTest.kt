package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.model.data.Event
import de.geosphere.speechplaning.data.repository.services.FirestoreService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate

internal class CongregationEventRepositoryTest : BehaviorSpec({

    lateinit var firestoreService: FirestoreService
    lateinit var repository: CongregationEventRepository

    val testDistrictId = "testDistrictId001"
    val testCongregationId = "testCongId002"
    val testEventId = "testEventId003"
    lateinit var testEvent: CongregationEvent
    lateinit var newEventWithoutId: CongregationEvent

    val districtsCollectionName = "districts"
    val congregationsSubcollectionName = "congregations"
    val congregationEventsSubcollectionName = "congregationEvents"

    val expectedParentCollectionPathForEvent =
        "$districtsCollectionName/$testDistrictId/$congregationsSubcollectionName"
    val expectedParentDocumentIdForEvent = testCongregationId

    beforeEach {
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

    given("SaveEvent") {
        `when`("saving a new event") {
            then("it should call firestoreService add and return new id") {
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

                resultId shouldBe generatedId
                coVerify {
                    firestoreService.addDocumentToSubcollection(
                        expectedParentCollectionPathForEvent,
                        expectedParentDocumentIdForEvent,
                        congregationEventsSubcollectionName,
                        newEventWithoutId
                    )
                }
            }

            then("it should throw an exception if firestoreService add fails") {
                val errorMessage = "Firestore add operation failed"
                coEvery {
                    firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    repository.saveEvent(testDistrictId, testCongregationId, newEventWithoutId)
                }
                exception.message shouldBe "Failed to save entity '[new]' in subcollection " +
                    "'$congregationEventsSubcollectionName' under parent '$expectedParentDocumentIdForEvent' " +
                    "in '$expectedParentCollectionPathForEvent'"
                exception.cause?.message shouldBe errorMessage
            }
        }

        `when`("saving an existing event") {
            then("it should call firestoreService set and return existing id") {
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

                resultId shouldBe testEvent.id
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

            then("it should throw an exception if firestoreService set fails") {
                val errorMessage = "Firestore set operation failed"
                coEvery {
                    firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    repository.saveEvent(testDistrictId, testCongregationId, testEvent)
                }
                exception.message shouldContain "Failed to save entity '${testEvent.id}' in subcollection " +
                    "'$congregationEventsSubcollectionName' under parent '$expectedParentDocumentIdForEvent' in " +
                    "'$expectedParentCollectionPathForEvent'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("GetEventById") {
        `when`("event is found") {
            then("it should return the event") {
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

                result shouldBe testEvent
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
        }

        `when`("eventId is blank") {
            then("it should return null") {
                val result = repository.getEventById(testDistrictId, testCongregationId, "")
                result.shouldBeNull()
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
        }

        `when`("firestoreService returns null") {
            then("it should return null") {
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

                result.shouldBeNull()
            }
        }

        `when`("firestoreService fails") {
            then("it should throw an exception") {
                val errorMessage = "Firestore get operation failed"
                coEvery {
                    firestoreService.getDocumentFromSubcollection(
                        any(), any(), any(), eq(testEventId), eq(CongregationEvent::class.java)
                    )
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    repository.getEventById(testDistrictId, testCongregationId, testEventId)
                }
                exception.message shouldContain "Failed to get entity '$testEventId' from subcollection " +
                    "'$congregationEventsSubcollectionName' under parent '$expectedParentDocumentIdForEvent' " +
                    "in '$expectedParentCollectionPathForEvent'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("GetAllEventsForCongregation") {
        `when`("called") {
            then("it should call firestoreService and return events list") {
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

                result shouldBe expectedEvents
                coVerify {
                    firestoreService.getDocumentsFromSubcollection(
                        expectedParentCollectionPathForEvent,
                        expectedParentDocumentIdForEvent,
                        congregationEventsSubcollectionName,
                        CongregationEvent::class.java
                    )
                }
            }

            then("it should return an empty list if firestoreService returns an empty list") {
                coEvery {
                    firestoreService.getDocumentsFromSubcollection(
                        any(), any(), any(), eq(CongregationEvent::class.java)
                    )
                } returns emptyList()

                val result = repository.getAllEventsForCongregation(testDistrictId, testCongregationId)

                result.size shouldBe 0
            }

            then("it should throw an exception if firestoreService fails") {
                val errorMessage = "Firestore get all operation failed"
                coEvery {
                    firestoreService.getDocumentsFromSubcollection(
                        any(), any(), any(), eq(CongregationEvent::class.java)
                    )
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    repository.getAllEventsForCongregation(testDistrictId, testCongregationId)
                }
                exception.message shouldContain "Failed to get all entities from subcollection " +
                    "'$congregationEventsSubcollectionName' under parent '$expectedParentDocumentIdForEvent' " +
                    "in '$expectedParentCollectionPathForEvent'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("DeleteEvent") {
        `when`("deleting an event") {
            then("it should call firestoreService with correct paths") {
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

            then("it should throw an exception if firestoreService fails") {
                val errorMessage = "Firestore delete operation failed"
                coEvery {
                    firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    repository.deleteEvent(testDistrictId, testCongregationId, testEventId)
                }
                exception.message shouldContain "Failed to delete entity '$testEventId' from subcollection " +
                    "'$congregationEventsSubcollectionName' under parent '$expectedParentDocumentIdForEvent' in " +
                    "'$expectedParentCollectionPathForEvent'"
                exception.cause?.message shouldBe errorMessage
            }

            then("it should throw an IllegalArgumentException when eventId is blank") {
                val exception = shouldThrow<IllegalArgumentException> {
                    repository.deleteEvent(testDistrictId, testCongregationId, "")
                }
                exception.message shouldBe "Document ID cannot be blank for deletion."
                coVerify(
                    exactly = 0
                ) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
            }
        }
    }

    given("HelperMethodTests") {
        `when`("extracting id from entity") {
            then("it should return the entity id") {
                repository.extractIdFromEntity(testEvent) shouldBe testEvent.id
                repository.extractIdFromEntity(newEventWithoutId) shouldBe newEventWithoutId.id
            }
        }

        `when`("building parent collection path") {
            then("it should return correct path with districtId and congregationId") {
                val path = repository.buildParentCollectionPath(testDistrictId, testCongregationId)
                path shouldBe expectedParentCollectionPathForEvent
            }

            then("it should throw an exception when parentIds count is not two") {
                shouldThrow<IllegalArgumentException> {
                    repository.buildParentCollectionPath(testDistrictId)
                }.message shouldBe "Expected districtId and congregationId as parentIds"

                shouldThrow<IllegalArgumentException> {
                    repository.buildParentCollectionPath()
                }.message shouldBe "Expected districtId and congregationId as parentIds"

                shouldThrow<IllegalArgumentException> {
                    repository.buildParentCollectionPath(testDistrictId, testCongregationId, "extraId")
                }.message shouldBe "Expected districtId and congregationId as parentIds"
            }
        }

        `when`("getting parent document id") {
            then("it should return congregationId when two parentIds are provided") {
                val result = repository.getParentDocumentId(testDistrictId, testCongregationId)
                result shouldBe testCongregationId
            }

            then("it should throw an exception when parentIds count is not two") {
                shouldThrow<IllegalArgumentException> {
                    repository.getParentDocumentId(testDistrictId)
                }.message shouldBe "Expected districtId and congregationId as parentIds"

                shouldThrow<IllegalArgumentException> {
                    repository.getParentDocumentId()
                }.message shouldBe "Expected districtId and congregationId as parentIds"

                shouldThrow<IllegalArgumentException> {
                    repository.getParentDocumentId(testDistrictId, testCongregationId, "extraId")
                }.message shouldBe "Expected districtId and congregationId as parentIds"
            }
        }
    }
})
