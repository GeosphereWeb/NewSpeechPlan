package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

internal class CongregationRepositoryTest : BehaviorSpec({

    lateinit var subcollectionActions: ISubcollectionActions
    lateinit var flowActions: IFlowActions
    lateinit var congregationRepository: CongregationRepository

    val districtId = "testDistrictId"
    val congregationId = "testCongregationId"
    val testCongregation = Congregation(id = congregationId, name = "Test Congregation")
    val congregationsSubcollectionName = "congregations"
    val districtsCollectionName = "districts"

    beforeEach {
        subcollectionActions = mockk(relaxed = true)
        flowActions = mockk(relaxed = true)
        congregationRepository = CongregationRepository(subcollectionActions, flowActions)
    }

    given("SaveCongregation") {
        `when`("saving a new congregation") {
            then("it should call subcollectionActions with correct paths and data") {
                val congregationToSave = testCongregation.copy(id = "") // Neue Congregation
                val expectedGeneratedId = "newCongregationId"

                coEvery {
                    subcollectionActions.addDocumentToSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        data = congregationToSave
                    )
                } returns expectedGeneratedId

                val resultId = congregationRepository.saveCongregation(districtId, congregationToSave)

                resultId shouldBe expectedGeneratedId
                coVerify {
                    subcollectionActions.addDocumentToSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        data = congregationToSave
                    )
                }
            }

            then("it should throw an exception if subcollectionActions add fails") {
                val congregationToSave = testCongregation.copy(id = "")
                val errorMessage = "Firestore add error"
                coEvery {
                    subcollectionActions.addDocumentToSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    congregationRepository.saveCongregation(districtId, congregationToSave)
                }
                exception.message shouldBe "Failed to save entity '[new]' in subcollection " +
                    "'$congregationsSubcollectionName' under parent '$districtId' in '$districtsCollectionName'"
                exception.cause?.message shouldBe errorMessage
            }
        }

        `when`("saving an existing congregation") {
            then("it should call subcollectionActions with correct paths and data") {
                coEvery {
                    subcollectionActions.setDocumentInSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = testCongregation.id,
                        data = testCongregation
                    )
                } returns Unit

                val resultId = congregationRepository.saveCongregation(districtId, testCongregation)

                resultId shouldBe testCongregation.id
                coVerify {
                    subcollectionActions.setDocumentInSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = testCongregation.id,
                        data = testCongregation
                    )
                }
            }

            then("it should throw an exception if subcollectionActions set fails") {
                val errorMessage = "Firestore set error"
                coEvery {
                    subcollectionActions.setDocumentInSubcollection(any(), any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    congregationRepository.saveCongregation(districtId, testCongregation)
                }
                exception.message shouldBe "Failed to save entity '${testCongregation.id}' in subcollection " +
                    "'$congregationsSubcollectionName' under parent '$districtId' in '$districtsCollectionName'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("DeleteCongregation") {
        `when`("deleting a congregation") {
            then("it should call subcollectionActions with correct paths") {
                coEvery {
                    subcollectionActions.deleteDocumentFromSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = congregationId
                    )
                } returns Unit

                congregationRepository.deleteCongregation(districtId, congregationId)

                coVerify {
                    subcollectionActions.deleteDocumentFromSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = congregationId
                    )
                }
            }

            then("it should throw an exception if subcollectionActions fails") {
                val errorMessage = "Firestore delete error"
                coEvery {
                    subcollectionActions.deleteDocumentFromSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    congregationRepository.deleteCongregation(districtId, congregationId)
                }
                exception.message shouldContain "Failed to delete entity '$congregationId' from subcollection " +
                    "'$congregationsSubcollectionName' under parent '$districtId' in '$districtsCollectionName'"
                exception.cause?.message shouldBe errorMessage
            }

            then("it should throw an IllegalArgumentException when congregationId is blank") {
                val exception = shouldThrow<IllegalArgumentException> {
                    congregationRepository.deleteCongregation(districtId, "")
                }
                exception.message shouldBe "Document ID cannot be blank for deletion."
                coVerify(
                    exactly = 0
                ) { subcollectionActions.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
            }
        }
    }

    given("ExtractIdFromEntityTest") {
        `when`("extracting id from entity") {
            then("it should return the entity id") {
                congregationRepository.extractIdFromEntity(testCongregation) shouldBe testCongregation.id
            }
        }
    }

    given("HelperMethods") {
        `when`("building parent collection path") {
            then("it should return correct path with one parentId") {
                val path = congregationRepository.buildParentCollectionPath(districtId)
                path shouldBe "districts"
            }

            then("it should throw an exception when parentIds count is not one") {
                shouldThrow<IllegalArgumentException> {
                    congregationRepository.buildParentCollectionPath()
                }
                shouldThrow<IllegalArgumentException> {
                    congregationRepository.buildParentCollectionPath("id1", "id2")
                }
            }
        }

        `when`("getting parent document id") {
            then("it should return districtId when one parentId is provided") {
                val result = congregationRepository.getParentDocumentId(districtId)
                result shouldBe districtId
            }

            then("it should throw an exception when parentIds count is not one") {
                shouldThrow<IllegalArgumentException> {
                    congregationRepository.getParentDocumentId()
                }
                shouldThrow<IllegalArgumentException> {
                    congregationRepository.getParentDocumentId("id1", "id2")
                }
            }
        }
    }
})
