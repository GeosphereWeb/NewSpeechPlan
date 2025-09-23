package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.data.model.Speaker
import de.geosphere.speechplaning.data.services.FirestoreService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class SpeakerRepositoryTest {

    private lateinit var firestoreService: FirestoreService
    private lateinit var speakerRepository: SpeakerRepository

    private val districtId = "testDistrictId"
    private val congregationId = "testCongregationId"
    private val speakerId = "testSpeakerId"
    private val testSpeaker = Speaker(
        id = speakerId,
        nameFirst = "Max",
        nameLast = "Mustermann",
        // Weitere Felder hier initialisieren, falls für Tests relevant
    )

    private val speakersSubcollectionName = "speakers"
    private val expectedParentCollectionPath = "districts/$districtId/congregations"

    @BeforeEach
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        speakerRepository = SpeakerRepository(firestoreService)
    }

    @Nested
    inner class SaveSpeaker {
        @Test
        fun `saveSpeaker for new speaker should call firestoreService with correct paths and data`() = runTest {
            val newSpeaker = testSpeaker.copy(id = "")
            val expectedGeneratedId = "newGeneratedSpeakerId"

            coEvery {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    data = newSpeaker
                )
            } returns expectedGeneratedId

            val resultId = speakerRepository.saveSpeaker(districtId, congregationId, newSpeaker)

            assertEquals(expectedGeneratedId, resultId)
            coVerify {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    data = newSpeaker
                )
            }
        }

        @Test
        fun `saveSpeaker for existing speaker should call firestoreService with correct paths and data`() = runTest {
            coEvery {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    documentId = testSpeaker.id,
                    data = testSpeaker
                )
            } returns Unit

            val resultId = speakerRepository.saveSpeaker(districtId, congregationId, testSpeaker)

            assertEquals(testSpeaker.id, resultId)
            coVerify {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    documentId = testSpeaker.id,
                    data = testSpeaker
                )
            }
        }

        @Test
        fun `saveSpeaker for new entity should throw exception if firestoreService add fails`() = runTest {
            val newSpeaker = testSpeaker.copy(id = "")
            val errorMessage = "Firestore add error"
            coEvery {
                firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                speakerRepository.saveSpeaker(districtId, congregationId, newSpeaker)
            }
            assertEquals(
                "Failed to save entity '[new]' in subcollection '$speakersSubcollectionName' " +
                    "under parent '$congregationId' in '$expectedParentCollectionPath'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }

        @Test
        fun `saveSpeaker for existing entity should throw exception if firestoreService set fails`() = runTest {
            val errorMessage = "Firestore set error"
            coEvery {
                firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                speakerRepository.saveSpeaker(districtId, congregationId, testSpeaker)
            }
            assertEquals(
                "Failed to save entity '${testSpeaker.id}' in subcollection '$speakersSubcollectionName' " +
                    "under parent '$congregationId' in '$expectedParentCollectionPath'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class GetSpeakersForCongregation {
        @Test
        fun `getSpeakersForCongregation should call firestoreService with correct paths`() = runTest {
            val expectedSpeakers = listOf(testSpeaker, testSpeaker.copy(id = "otherSpeakerId"))

            coEvery {
                firestoreService.getDocumentsFromSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    objectClass = Speaker::class.java
                )
            } returns expectedSpeakers

            val result = speakerRepository.getSpeakersForCongregation(districtId, congregationId)

            assertEquals(expectedSpeakers, result)
            coVerify {
                firestoreService.getDocumentsFromSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    objectClass = Speaker::class.java
                )
            }
        }

        @Test
        fun `getSpeakersForCongregation should throw exception if firestoreService fails`() = runTest {
            val errorMessage = "Firestore get all error"
            coEvery {
                firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(Speaker::class.java))
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                speakerRepository.getSpeakersForCongregation(districtId, congregationId)
            }
            assertEquals(
                "Failed to get all entities from subcollection '$speakersSubcollectionName' under parent " +
                    "'$congregationId' in '$expectedParentCollectionPath'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class DeleteSpeaker {
        @Test
        fun `deleteSpeaker should call firestoreService with correct paths`() = runTest {
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    documentId = speakerId
                )
            } returns Unit

            speakerRepository.deleteSpeaker(districtId, congregationId, speakerId)

            coVerify {
                firestoreService.deleteDocumentFromSubcollection(
                    parentCollection = expectedParentCollectionPath,
                    parentId = congregationId,
                    subcollection = speakersSubcollectionName,
                    documentId = speakerId
                )
            }
        }

        @Test
        fun `deleteSpeaker should throw exception if firestoreService fails`() = runTest {
            val errorMessage = "Firestore delete error"
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                speakerRepository.deleteSpeaker(districtId, congregationId, speakerId)
            }
            assertEquals(
                "Failed to delete entity '$speakerId' from subcollection '$speakersSubcollectionName' " +
                    "under parent '$congregationId' in '$expectedParentCollectionPath'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }

        @Test
        fun `deleteSpeaker should throw IllegalArgumentException when speakerId is blank`() {
            assertThrows<IllegalArgumentException> {
                runTest { speakerRepository.deleteSpeaker(districtId, congregationId, "") }
            }.apply {
                assertEquals("Document ID cannot be blank for deletion.", message)
            }
            coVerify(exactly = 0) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
        }
    }

    @Nested
    inner class ExtractIdFromEntityTest {
        @Test
        fun `extractIdFromEntity should return entity id`() {
            assertEquals(testSpeaker.id, speakerRepository.extractIdFromEntity(testSpeaker))
        }
    }
    
    @Nested
    inner class HelperMethods {
        @Test
        fun `buildParentCollectionPath returns correct path with two parentIds`() {
            val path = speakerRepository.buildParentCollectionPath(districtId, congregationId)
            assertEquals("districts/$districtId/congregations", path)
        }

        @Test
        fun `buildParentCollectionPath throws exception when parentIds count is not two`() {
            assertThrows<IllegalArgumentException> {
                speakerRepository.buildParentCollectionPath("singleId")
            }
            assertThrows<IllegalArgumentException> {
                speakerRepository.buildParentCollectionPath("id1", "id2", "id3")
            }
        }

        @Test
        fun `getParentDocumentId returns congregationId when two parentIds are provided`() {
            val result = speakerRepository.getParentDocumentId(districtId, congregationId)
            assertEquals(congregationId, result)
        }

        @Test
        fun `getParentDocumentId throws exception when parentIds count is not two`() {
            assertThrows<IllegalArgumentException> {
                speakerRepository.getParentDocumentId("singleId")
            }
            assertThrows<IllegalArgumentException> {
                speakerRepository.getParentDocumentId("id1", "id2", "id3")
            }
        }
    }
}
