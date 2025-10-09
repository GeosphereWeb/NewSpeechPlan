package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.data.model.Congregation
import de.geosphere.speechplaning.data.services.FirestoreService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

internal class CongregationRepositoryTest : BehaviorSpec({

    lateinit var firestoreService: FirestoreService
    lateinit var congregationRepository: CongregationRepository

     val districtId = "testDistrictId"
     val congregationId = "testCongregationId"
     val testCongregation = Congregation(id = congregationId, name = "Test Congregation")
     val congregationsSubcollectionName = "congregations"
     val districtsCollectionName = "districts"

    beforeEach {
        firestoreService = mockk(relaxed = true)
        congregationRepository = CongregationRepository(firestoreService)
    }

    given("SaveCongregation") {
        `when`("saving a new congregation") {
            then("it should call firestoreService with correct paths and data") {
                val congregationToSave = testCongregation.copy(id = "") // Neue Congregation
                val expectedGeneratedId = "newCongregationId"

                coEvery {
                    firestoreService.addDocumentToSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        data = congregationToSave
                    )
                } returns expectedGeneratedId

                val resultId = congregationRepository.saveCongregation(districtId, congregationToSave)

                resultId shouldBe expectedGeneratedId
                coVerify {
                    firestoreService.addDocumentToSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        data = congregationToSave
                    )
                }
            }

            then("it should throw an exception if firestoreService add fails") {
                val congregationToSave = testCongregation.copy(id = "")
                val errorMessage = "Firestore add error"
                coEvery {
                    firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    congregationRepository.saveCongregation(districtId, congregationToSave)
                }
                exception.message shouldBe "Failed to save entity '[new]' in subcollection " +
                    "'$congregationsSubcollectionName' under parent '${districtId}' in '$districtsCollectionName'"
                exception.cause?.message shouldBe errorMessage
            }
        }

        `when`("saving an existing congregation") {
            then("it should call firestoreService with correct paths and data") {
                coEvery {
                    firestoreService.setDocumentInSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = testCongregation.id,
                        data = testCongregation
                    )
                } returns Unit // Firestore setDocumentInSubcollection gibt nichts zurück

                val resultId = congregationRepository.saveCongregation(districtId, testCongregation)

                resultId shouldBe testCongregation.id
                coVerify {
                    firestoreService.setDocumentInSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = testCongregation.id,
                        data = testCongregation
                    )
                }
            }

            then("it should throw an exception if firestoreService set fails") {
                val errorMessage = "Firestore set error"
                coEvery {
                    firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
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

    given("GetCongregationsForDistrict") {
        `when`("called") {
            then("it should call firestoreService with correct paths") {
                val expectedCongregations = listOf(testCongregation, testCongregation.copy(id = "otherId"))

                coEvery {
                    firestoreService.getDocumentsFromSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        objectClass = Congregation::class.java
                    )
                } returns expectedCongregations

                val result = congregationRepository.getCongregationsForDistrict(districtId)

                result shouldBe expectedCongregations
                coVerify {
                    firestoreService.getDocumentsFromSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        objectClass = Congregation::class.java
                    )
                }
            }

            then("it should throw an exception if firestoreService fails") {
                val errorMessage = "Firestore get all error"
                coEvery {
                    firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(Congregation::class.java))
                } throws RuntimeException(errorMessage)

                val exception = shouldThrow<RuntimeException> {
                    congregationRepository.getCongregationsForDistrict(districtId)
                }
                exception.message shouldBe "Failed to get all entities from subcollection " +
                    "'$congregationsSubcollectionName' under parent '$districtId' in '$districtsCollectionName'"
                exception.cause?.message shouldBe errorMessage
            }
        }
    }

    given("DeleteCongregation") {
        `when`("deleting a congregation") {
            then("it should call firestoreService with correct paths") {
                coEvery {
                    firestoreService.deleteDocumentFromSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = congregationId
                    )
                } returns Unit

                congregationRepository.deleteCongregation(districtId, congregationId)

                coVerify {
                    firestoreService.deleteDocumentFromSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        documentId = congregationId
                    )
                }
            }

            then("it should throw an exception if firestoreService fails") {
                val errorMessage = "Firestore delete error"
                coEvery {
                    firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
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
                coVerify(exactly = 0) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
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
