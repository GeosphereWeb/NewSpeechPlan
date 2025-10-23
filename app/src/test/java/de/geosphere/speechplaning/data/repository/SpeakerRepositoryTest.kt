package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.data.model.Speaker
import de.geosphere.speechplaning.data.model.repository.SpeakerRepository
import de.geosphere.speechplaning.data.services.FirestoreService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

internal class SpeakerRepositoryTest : BehaviorSpec({

    lateinit var firestoreService: FirestoreService
    lateinit var speakerRepository: SpeakerRepository

    val districtId = "testDistrictId"
    val congregationId = "testCongregationId"
    val speakerId = "testSpeakerId"
    val testSpeaker = Speaker(
        id = speakerId,
        nameFirst = "Max",
        nameLast = "Mustermann",
        // Weitere Felder hier initialisieren, falls für Tests relevant
    )

    val speakersSubcollectionName = "speakers"
    val expectedParentCollectionPath = "districts/$districtId/congregations"

    beforeEach {
        firestoreService = mockk(relaxed = true)
        speakerRepository = SpeakerRepository(firestoreService)
    }

    given("SaveSpeaker") {
        `when`("saving a new speaker") {
            then("it should call firestoreService with correct paths and data") {
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

                resultId shouldBe expectedGeneratedId
                coVerify {
                    firestoreService.addDocumentToSubcollection(
                        parentCollection = expectedParentCollectionPath,
                        parentId = congregationId,
                        subcollection = speakersSubcollectionName,
                        data = newSpeaker
                    )
                }
            }

            then("it should throw an exception if firestoreService add fails") {
                val newSpeaker = testSpeaker.copy(id = "")
                val errorMessage = "Firestore add error"
                coEvery {
                    firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    speakerRepository.saveSpeaker(districtId, congregationId, newSpeaker)
                }
                exception.message shouldBe "Failed to save entity '[new]' in subcollection " +
                    "'$speakersSubcollectionName' under parent '$congregationId' in '$expectedParentCollectionPath'"
                exception.cause?.message shouldBe errorMessage
            }
        }

        `when`("saving an existing speaker") {
            then("it should call firestoreService with correct paths and data") {
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

                resultId shouldBe testSpeaker.id
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

            then("it should throw an exception if firestoreService set fails") {
                val errorMessage = "Firestore set error"
                coEvery {
                    firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    speakerRepository.saveSpeaker(districtId, congregationId, testSpeaker)
                }
                exception.message shouldContain "Failed to save entity '${testSpeaker.id}' in subcollection " +
                    "'$speakersSubcollectionName' under parent '$congregationId' in '$expectedParentCollectionPath'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("GetSpeakersForCongregation") {
        `when`("called") {
            then("it should call firestoreService with correct paths") {
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

                result shouldBe expectedSpeakers
                coVerify {
                    firestoreService.getDocumentsFromSubcollection(
                        parentCollection = expectedParentCollectionPath,
                        parentId = congregationId,
                        subcollection = speakersSubcollectionName,
                        objectClass = Speaker::class.java
                    )
                }
            }

            then("it should throw an exception if firestoreService fails") {
                val errorMessage = "Firestore get all error"
                coEvery {
                    firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(Speaker::class.java))
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    speakerRepository.getSpeakersForCongregation(districtId, congregationId)
                }
                exception.message shouldContain "Failed to get all entities from subcollection " +
                    "'$speakersSubcollectionName' under parent '$congregationId' in '$expectedParentCollectionPath'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("DeleteSpeaker") {
        `when`("deleting a speaker") {
            then("it should call firestoreService with correct paths") {
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

            then("it should throw an exception if firestoreService fails") {
                val errorMessage = "Firestore delete error"
                coEvery {
                    firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    speakerRepository.deleteSpeaker(districtId, congregationId, speakerId)
                }
                exception.message shouldContain "Failed to delete entity '$speakerId' from subcollection " +
                    "'$speakersSubcollectionName' under parent '$congregationId' in '$expectedParentCollectionPath'"
                exception.cause?.message shouldBe errorMessage
            }

            then("it should throw an IllegalArgumentException when speakerId is blank") {
                val exception = shouldThrow<IllegalArgumentException> {
                    speakerRepository.deleteSpeaker(districtId, congregationId, "")
                }
                exception.message shouldBe "Document ID cannot be blank for deletion."
                coVerify(
                    exactly = 0
                ) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
            }
        }
    }

    given("ExtractIdFromEntityTest") {
        `when`("extracting id from entity") {
            then("it should return the entity id") {
                speakerRepository.extractIdFromEntity(testSpeaker) shouldBe testSpeaker.id
            }
        }
    }

    given("HelperMethods") {
        `when`("building parent collection path") {
            then("it should return correct path with two parentIds") {
                val path = speakerRepository.buildParentCollectionPath(districtId, congregationId)
                path shouldBe "districts/$districtId/congregations"
            }

            then("it should throw an exception when parentIds count is not two") {
                shouldThrow<IllegalArgumentException> {
                    speakerRepository.buildParentCollectionPath("singleId")
                }
                shouldThrow<IllegalArgumentException> {
                    speakerRepository.buildParentCollectionPath("id1", "id2", "id3")
                }
            }
        }

        `when`("getting parent document id") {
            then("it should return congregationId when two parentIds are provided") {
                val result = speakerRepository.getParentDocumentId(districtId, congregationId)
                result shouldBe congregationId
            }

            then("it should throw an exception when parentIds count is not two") {
                shouldThrow<IllegalArgumentException> {
                    speakerRepository.getParentDocumentId("singleId")
                }
                shouldThrow<IllegalArgumentException> {
                    speakerRepository.getParentDocumentId("id1", "id2", "id3")
                }
            }
        }
    }
})
