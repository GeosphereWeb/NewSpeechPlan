package de.geosphere.speechplaning.data.repository

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

internal class SpeakerRepositoryTest : BehaviorSpec({

    lateinit var subcollectionActions: ISubcollectionActions
    lateinit var flowActions: IFlowActions
    lateinit var speakerRepository: SpeakerRepository

    val districtId = "testDistrictId"
    val congregationId = "testCongregationId"
    val speakerId = "testSpeakerId"
    val testSpeaker = Speaker(
        id = speakerId,
        firstName = "Max",
        lastName = "Mustermann",
    )

    val speakersSubcollectionName = "speakers"
    val expectedParentCollectionPath = "districts/$districtId/congregations"

    beforeEach {
        subcollectionActions = mockk(relaxed = true)
        flowActions = mockk(relaxed = true)
        speakerRepository = SpeakerRepository(subcollectionActions, flowActions)
    }

    given("SaveSpeaker") {
        `when`("saving a new speaker") {
            then("it should call subcollectionActions with correct paths and data") {
                val newSpeaker = testSpeaker.copy(id = "")
                val expectedGeneratedId = "newGeneratedSpeakerId"

                coEvery {
                    subcollectionActions.addDocumentToSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        newSpeaker
                    )
                } returns expectedGeneratedId

                val resultId = speakerRepository.saveSpeaker(districtId, congregationId, newSpeaker)

                resultId shouldBe expectedGeneratedId
                coVerify {
                    subcollectionActions.addDocumentToSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        newSpeaker
                    )
                }
            }

            then("it should throw an exception if subcollectionActions add fails") {
                val newSpeaker = testSpeaker.copy(id = "")
                val errorMessage = "Firestore add error"
                coEvery {
                    subcollectionActions.addDocumentToSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        newSpeaker
                    )
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
            then("it should call subcollectionActions with correct paths and data") {
                coEvery {
                    subcollectionActions.setDocumentInSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        testSpeaker.id,
                        testSpeaker
                    )
                } returns Unit

                val resultId = speakerRepository.saveSpeaker(districtId, congregationId, testSpeaker)

                resultId shouldBe testSpeaker.id
                coVerify {
                    subcollectionActions.setDocumentInSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        testSpeaker.id,
                        testSpeaker
                    )
                }
            }
        }
    }

    given("GetSpeakersForCongregation") {
        `when`("observing speakers flow") {
            then("it should call getAllFlow with correct parameters") {
                val expectedSpeakers = listOf(testSpeaker)
                // Da getAllFlow in der Basisklasse FirestoreSubcollectionRepository noch nicht implementiert ist
                // (wirft UnsupportedOperationException), verwenden wir einen spyk, um das Verhalten
                // für diesen Test zu simulieren, ohne die Repository-Klasse zu ändern.
                val spiedRepository = spyk(speakerRepository)
                every { spiedRepository.getAllFlow(districtId, congregationId) } returns flowOf(expectedSpeakers)

                spiedRepository.getSpeakersForCongregation(districtId, congregationId).test {
                    awaitItem() shouldBe expectedSpeakers
                    awaitComplete()
                }

                verify { spiedRepository.getAllFlow(districtId, congregationId) }
            }
        }
    }

    given("DeleteSpeaker") {
        `when`("deleting a speaker") {
            then("it should call subcollectionActions with correct paths") {
                coEvery {
                    subcollectionActions.deleteDocumentFromSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        speakerId
                    )
                } returns Unit

                speakerRepository.deleteSpeaker(districtId, congregationId, speakerId)

                coVerify {
                    subcollectionActions.deleteDocumentFromSubcollection(
                        expectedParentCollectionPath,
                        congregationId,
                        speakersSubcollectionName,
                        speakerId
                    )
                }
            }

            then("it should throw an IllegalArgumentException when speakerId is blank") {
                val exception = shouldThrow<IllegalArgumentException> {
                    speakerRepository.deleteSpeaker(districtId, congregationId, "")
                }
                exception.message shouldBe "Document ID cannot be blank for deletion."
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
        }

        `when`("getting parent document id") {
            then("it should return congregationId when two parentIds are provided") {
                val result = speakerRepository.getParentDocumentId(districtId, congregationId)
                result shouldBe congregationId
            }
        }
    }
})
