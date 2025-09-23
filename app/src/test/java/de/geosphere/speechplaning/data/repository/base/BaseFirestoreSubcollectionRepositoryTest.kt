package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.data.services.FirestoreService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal data class TestData(val id: String, val data: String)

internal class TestRepository(
    firestoreService: FirestoreService,
    subcollectionName: String
) : BaseFirestoreSubcollectionRepository<TestData>(
    firestoreService,
    subcollectionName,
    TestData::class.java
) {
    override fun extractIdFromEntity(entity: TestData): String = entity.id
    override fun buildParentCollectionPath(vararg parentIds: String): String = "parentCollection/${parentIds[0]}"
    override fun getParentDocumentId(vararg parentIds: String): String = parentIds[1]

    // Korrektur: Sichtbarkeit von isEntityIdBlank für den Test auf public erhöhen
    public override fun isEntityIdBlank(id: String): Boolean = super.isEntityIdBlank(id)
}

internal class BaseFirestoreSubcollectionRepositoryTest {

    private lateinit var firestoreService: FirestoreService
    private lateinit var testRepository: TestRepository
    private lateinit var spyTestRepository: TestRepository // Für das Testen von open funcs

    private val testSubcollectionName = "testSubcollection"
    private val parentId1 = "parent1"
    private val parentId2 = "childParent1"

    @BeforeEach
    fun setUp() {
        firestoreService = mockk(relaxed = true)
        // Verwende die TestRepository-Implementierung, die die öffentliche isEntityIdBlank hat
        testRepository = TestRepository(firestoreService, testSubcollectionName)
        spyTestRepository = spyk(testRepository) // Spioniere die Instanz, die die öffentliche Methode hat
    }

    @Nested
    inner class Save {
        @Test
        fun `save new entity should call addDocumentToSubcollection and return new id`() = runTest {
            val newEntity = TestData("", "newData")
            val generatedId = "newGeneratedId"
            val expectedParentPath = "parentCollection/$parentId1"

            // Jetzt sollte der Spy korrekt auf die öffentliche Methode zugreifen können
            every { spyTestRepository.isEntityIdBlank(newEntity.id) } returns true
            coEvery {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    data = newEntity
                )
            } returns generatedId

            val resultId = spyTestRepository.save(newEntity, parentId1, parentId2)

            assertEquals(generatedId, resultId)
            coVerify {
                firestoreService.addDocumentToSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    data = newEntity
                )
            }
        }

        @Test
        fun `save existing entity should call setDocumentInSubcollection and return existing id`() = runTest {
            val existingEntity = TestData("existingId", "updatedData")
            val expectedParentPath = "parentCollection/$parentId1"

            every { spyTestRepository.isEntityIdBlank(existingEntity.id) } returns false
            coEvery {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    documentId = existingEntity.id,
                    data = existingEntity
                )
            } returns Unit // void

            val resultId = spyTestRepository.save(existingEntity, parentId1, parentId2)

            assertEquals(existingEntity.id, resultId)
            coVerify {
                firestoreService.setDocumentInSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    documentId = existingEntity.id,
                    data = existingEntity
                )
            }
        }

        @Test
        fun `save should throw RuntimeException when firestoreService fails for new entity`() = runTest {
            val newEntity = TestData("", "someData")
            val expectedParentPath = "parentCollection/$parentId1"
            val exceptionMessage = "Firestore error on add"

            every { spyTestRepository.isEntityIdBlank(newEntity.id) } returns true
            coEvery {
                firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
            } throws Exception(exceptionMessage)

            val exception = assertThrows<RuntimeException> {
                spyTestRepository.save(newEntity, parentId1, parentId2)
            }
            assertTrue(
                exception.message?.contains(
                    "Failed to save entity '[new]' in subcollection " +
                        "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                ) ?: false
            )
            assertTrue(exception.cause?.message == exceptionMessage)
        }

        @Test
        fun `save should throw RuntimeException when firestoreService fails for existing entity`() = runTest {
            val existingEntity = TestData("someId", "someData")
            val expectedParentPath = "parentCollection/$parentId1"
            val exceptionMessage = "Firestore error on set"

            every { spyTestRepository.isEntityIdBlank(existingEntity.id) } returns false
            coEvery {
                firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
            } throws Exception(exceptionMessage)

            val exception = assertThrows<RuntimeException> {
                spyTestRepository.save(existingEntity, parentId1, parentId2)
            }
            assertTrue(
                exception.message?.contains(
                    "Failed to save entity 'someId' in subcollection " +
                        "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                ) ?: false
            )
            assertTrue(exception.cause?.message == exceptionMessage)
        }
    }

    @Nested
    inner class GetAll {
        @Test
        fun `getAll should call getDocumentsFromSubcollection and return list`() = runTest {
            val expectedList = listOf(TestData("id1", "data1"), TestData("id2", "data2"))
            val expectedParentPath = "parentCollection/$parentId1"
            coEvery {
                firestoreService.getDocumentsFromSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    objectClass = TestData::class.java
                )
            } returns expectedList

            val resultList = testRepository.getAll(parentId1, parentId2) // Teste mit testRepository, nicht spy

            assertEquals(expectedList, resultList)
        }

        @Test
        fun `getAll should throw RuntimeException when firestoreService fails`() = runTest {
            val expectedParentPath = "parentCollection/$parentId1"
            val exceptionMessage = "Firestore error"
            coEvery {
                firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(TestData::class.java))
            } throws Exception(exceptionMessage)

            val exception = assertThrows<RuntimeException> {
                testRepository.getAll(parentId1, parentId2) // Teste mit testRepository, nicht spy
            }
            assertTrue(
                exception.message?.contains(
                    "Failed to get all entities from subcollection " +
                        "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                ) ?: false
            )
            assertTrue(exception.cause?.message == exceptionMessage)
        }
    }

    @Nested
    inner class GetById {
        @Test
        fun `getById should return entity when found`() = runTest {
            val entityId = "testId"
            val expectedEntity = TestData(entityId, "testData")
            val expectedParentPath = "parentCollection/$parentId1"
            coEvery {
                firestoreService.getDocumentFromSubcollection(
                    parentCollectionPath = expectedParentPath,
                    parentDocumentId = parentId2,
                    subcollectionName = testSubcollectionName,
                    documentId = entityId,
                    objectClass = TestData::class.java
                )
            } returns expectedEntity

            val result = testRepository.getById(entityId, parentId1, parentId2) // Teste mit testRepository, nicht spy

            assertEquals(expectedEntity, result)
        }

        @Test
        fun `getById should return null when entityId is blank`() = runTest {
            val result = testRepository.getById("", parentId1, parentId2) // Teste mit testRepository, nicht spy
            assertNull(result)
            coVerify(exactly = 0) {
                firestoreService.getDocumentFromSubcollection(
                    any(), // parentCollectionPath
                    any(), // parentDocumentId
                    any(), // subcollectionName
                    any(), // documentId
                    eq(TestData::class.java) // objectClass
                )
            }
        }

        @Test
        fun `getById should return null when firestoreService returns null`() = runTest {
            val entityId = "nonExistentId"
            val expectedParentPath = "parentCollection/$parentId1"
            coEvery {
                firestoreService.getDocumentFromSubcollection(
                    parentCollectionPath = expectedParentPath,
                    parentDocumentId = parentId2,
                    subcollectionName = testSubcollectionName,
                    documentId = entityId,
                    objectClass = TestData::class.java
                )
            } returns null

            val result = testRepository.getById(entityId, parentId1, parentId2) // Teste mit testRepository, nicht spy

            assertNull(result)
        }

        @Test
        fun `getById should throw RuntimeException when firestoreService fails`() = runTest {
            val entityId = "testId"
            val expectedParentPath = "parentCollection/$parentId1"
            val exceptionMessage = "Firestore error"
            coEvery {
                firestoreService.getDocumentFromSubcollection(any(), any(), any(), any(), eq(TestData::class.java))
            } throws Exception(exceptionMessage)

            val exception = assertThrows<RuntimeException> {
                testRepository.getById(entityId, parentId1, parentId2) // Teste mit testRepository, nicht spy
            }
            assertTrue(
                exception.message?.contains(
                    "Failed to get entity '$entityId' from subcollection " +
                        "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                ) ?: false
            )
            assertTrue(exception.cause?.message == exceptionMessage)
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `delete should call deleteDocumentFromSubcollection`() = runTest {
            val entityId = "testIdToDelete"
            val expectedParentPath = "parentCollection/$parentId1"
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    documentId = entityId
                )
            } returns Unit

            testRepository.delete(entityId, parentId1, parentId2) // Teste mit testRepository, nicht spy

            coVerify {
                firestoreService.deleteDocumentFromSubcollection(
                    parentCollection = expectedParentPath,
                    parentId = parentId2,
                    subcollection = testSubcollectionName,
                    documentId = entityId
                )
            }
        }

        @Test
        fun `delete should throw IllegalArgumentException when entityId is blank`() {
            val exception = assertThrows<IllegalArgumentException> {
                runTest { testRepository.delete("", parentId1, parentId2) } // Teste mit testRepository, nicht spy
            }
            assertEquals("Document ID cannot be blank for deletion.", exception.message)
            coVerify(exactly = 0) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
        }

        @Test
        fun `delete should throw RuntimeException when firestoreService fails`() = runTest {
            val entityId = "testId"
            val expectedParentPath = "parentCollection/$parentId1"
            val exceptionMessage = "Firestore error"
            coEvery {
                firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
            } throws Exception(exceptionMessage)

            val exception = assertThrows<RuntimeException> {
                testRepository.delete(entityId, parentId1, parentId2) // Teste mit testRepository, nicht spy
            }
            assertTrue(
                exception.message?.contains(
                    "Failed to delete entity '$entityId' from subcollection " +
                        "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                ) ?: false
            )
            assertTrue(exception.cause?.message == exceptionMessage)
        }
    }

    @Test
    fun `isEntityIdBlank default implementation in TestRepository works correctly`() {
        // Teste die öffentliche Methode in TestRepository direkt
        assertTrue(spyTestRepository.isEntityIdBlank(""))
        assertTrue(spyTestRepository.isEntityIdBlank("   "))
        assertTrue(!spyTestRepository.isEntityIdBlank("id"))
    }
}
