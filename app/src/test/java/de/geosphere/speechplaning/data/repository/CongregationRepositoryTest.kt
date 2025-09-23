package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.data.model.Congregation
import de.geosphere.speechplaning.data.services.FirestoreService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
// import io.mockk.spyk // Spy wird nicht mehr benötigt
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CongregationRepositoryTest {

    private lateinit var firestoreService: FirestoreService
    private lateinit var congregationRepository: CongregationRepository

    private val districtId = "testDistrictId"
    private val congregationId = "testCongregationId"
    private val testCongregation = Congregation(id = congregationId, name = "Test Congregation")
    private val congregationsSubcollectionName = "congregations"
    private val districtsCollectionName = "districts"

    @BeforeEach
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        congregationRepository = CongregationRepository(firestoreService)
        // spyCongregationRepository = spyk(congregationRepository, recordPrivateCalls = true) // Nicht mehr benötigt
    }

    @Nested
    inner class SaveCongregation {
        @Test
        fun `saveCongregation for new congregation should call firestoreService with correct paths and data`() =
            runTest {
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

                assertEquals(expectedGeneratedId, resultId)
                coVerify {
                    firestoreService.addDocumentToSubcollection(
                        parentCollection = districtsCollectionName,
                        parentId = districtId,
                        subcollection = congregationsSubcollectionName,
                        data = congregationToSave
                    )
                }
            }

        @Test
        fun `saveCongregation for existing congregation should call firestoreService with correct paths and data`() =
            runTest {
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

                assertEquals(testCongregation.id, resultId)
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

        @Test
        fun `saveCongregation for new entity should throw exception if firestoreService add fails`() = runTest {
            val congregationToSave = testCongregation.copy(id = "")
            val errorMessage = "Firestore add error"
            coEvery {
                firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                congregationRepository.saveCongregation(districtId, congregationToSave)
            }
            assertEquals(
                "Failed to save entity '[new]' in subcollection '$congregationsSubcollectionName' under parent " +
                    "'$districtId' in '$districtsCollectionName'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }

        @Test
        fun `saveCongregation for existing entity should throw exception if firestoreService set fails`() = runTest {
            val errorMessage = "Firestore set error"
            coEvery {
                firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                congregationRepository.saveCongregation(districtId, testCongregation)
            }
            assertEquals(
                "Failed to save entity '${testCongregation.id}' in subcollection '$congregationsSubcollectionName' " +
                    "under parent '$districtId' in '$districtsCollectionName'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class GetCongregationsForDistrict {
        @Test
        fun `getCongregationsForDistrict should call firestoreService with correct paths`() = runTest {
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

            assertEquals(expectedCongregations, result)
            coVerify {
                firestoreService.getDocumentsFromSubcollection(
                    parentCollection = districtsCollectionName,
                    parentId = districtId,
                    subcollection = congregationsSubcollectionName,
                    objectClass = Congregation::class.java
                )
            }
        }

        @Test
        fun `getCongregationsForDistrict should throw exception if firestoreService fails`() = runTest {
            val errorMessage = "Firestore get all error"
            coEvery {
                firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(Congregation::class.java))
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                congregationRepository.getCongregationsForDistrict(districtId)
            }
            assertEquals(
                "Failed to get all entities from subcollection '$congregationsSubcollectionName' under parent " +
                    "'$districtId' in '$districtsCollectionName'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }
    }

    @Nested
    inner class DeleteCongregation {
        @Test
        fun `deleteCongregation should call firestoreService with correct paths`() = runTest {
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

        @Test
        fun `deleteCongregation should throw exception if firestoreService fails`() = runTest {
            val errorMessage = "Firestore delete error"
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
            } throws RuntimeException(errorMessage)

            val exception = assertThrows<RuntimeException> {
                congregationRepository.deleteCongregation(districtId, congregationId)
            }
            assertEquals(
                "Failed to delete entity '$congregationId' from subcollection '$congregationsSubcollectionName' " +
                    "under parent '$districtId' in '$districtsCollectionName'",
                exception.message
            )
            assertEquals(errorMessage, exception.cause?.message)
        }

        @Test
        fun `deleteCongregation should throw IllegalArgumentException when congregationId is blank`() {
            assertThrows<IllegalArgumentException> {
                runTest { congregationRepository.deleteCongregation(districtId, "") }
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
            // Diese Methode ist in CongregationRepository effektiv 'internal override'
            // und kann direkt getestet werden.
            assertEquals(testCongregation.id, congregationRepository.extractIdFromEntity(testCongregation))
        }
    }

    @Nested
    inner class HelperMethods {
        @Test
        fun `buildParentCollectionPath returns correct path with one parentId`() {
            val path = congregationRepository.buildParentCollectionPath(districtId)
            assertEquals("districts", path)
        }

        @Test
        fun `buildParentCollectionPath throws exception when parentIds count is not one`() {
            assertThrows<IllegalArgumentException> {
                congregationRepository.buildParentCollectionPath() // 0 arguments
            }
            assertThrows<IllegalArgumentException> {
                congregationRepository.buildParentCollectionPath("id1", "id2") // 2 arguments
            }
        }

        @Test
        fun `getParentDocumentId returns districtId when one parentId is provided`() {
            val result = congregationRepository.getParentDocumentId(districtId)
            assertEquals(districtId, result)
        }

        @Test
        fun `getParentDocumentId throws exception when parentIds count is not one`() {
            assertThrows<IllegalArgumentException> {
                congregationRepository.getParentDocumentId() // 0 arguments
            }
            assertThrows<IllegalArgumentException> {
                congregationRepository.getParentDocumentId("id1", "id2") // 2 arguments
            }
        }
    }
}
