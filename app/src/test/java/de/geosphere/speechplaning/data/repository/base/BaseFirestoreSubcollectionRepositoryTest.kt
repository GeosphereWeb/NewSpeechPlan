package de.geosphere.speechplaning.data.repository.base

import de.geosphere.speechplaning.data.services.FirestoreService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk

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

internal class BaseFirestoreSubcollectionRepositoryTest : BehaviorSpec({

    lateinit var firestoreService: FirestoreService
    lateinit var testRepository: TestRepository
    lateinit var spyTestRepository: TestRepository // Für das Testen von open funcs

    val testSubcollectionName = "testSubcollection"
    val parentId1 = "parent1"
    val parentId2 = "childParent1"

    beforeEach {
        firestoreService = mockk(relaxed = true)
        // Verwende die TestRepository-Implementierung, die die öffentliche isEntityIdBlank hat
        testRepository = TestRepository(firestoreService, testSubcollectionName)
        spyTestRepository = spyk(testRepository) // Spioniere die Instanz, die die öffentliche Methode hat
    }

    given("Save") {
        `when`("saving a new entity") {
            then("it should call addDocumentToSubcollection and return new id") {
                val newEntity = TestData("", "newData")
                val generatedId = "newGeneratedId"
                val expectedParentPath = "parentCollection/$parentId1"

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

                resultId shouldBe generatedId
                coVerify {
                    firestoreService.addDocumentToSubcollection(
                        parentCollection = expectedParentPath,
                        parentId = parentId2,
                        subcollection = testSubcollectionName,
                        data = newEntity
                    )
                }
            }
        }

        `when`("saving an existing entity") {
            then("it should call setDocumentInSubcollection and return existing id") {
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

                resultId shouldBe existingEntity.id
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
        }

        `when`("firestoreService fails for a new entity") {
            then("it should throw a RuntimeException") {
                val newEntity = TestData("", "someData")
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error on add"

                every { spyTestRepository.isEntityIdBlank(newEntity.id) } returns true
                coEvery {
                    firestoreService.addDocumentToSubcollection(any(), any(), any(), any())
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    spyTestRepository.save(newEntity, parentId1, parentId2)
                }
                exception.message shouldContain "Failed to save entity '[new]' in subcollection '$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }

        `when`("firestoreService fails for an existing entity") {
            then("it should throw a RuntimeException") {
                val existingEntity = TestData("someId", "someData")
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error on set"

                every { spyTestRepository.isEntityIdBlank(existingEntity.id) } returns false
                coEvery {
                    firestoreService.setDocumentInSubcollection(any(), any(), any(), any(), any())
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    spyTestRepository.save(existingEntity, parentId1, parentId2)
                }
                exception.message shouldContain "Failed to save entity 'someId' in subcollection '$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }
    }

    given("GetAll") {
        `when`("getAll is called") {
            then("it should call getDocumentsFromSubcollection and return a list") {
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

                val resultList = testRepository.getAll(parentId1, parentId2)

                resultList shouldBe expectedList
            }
        }

        `when`("firestoreService fails during getAll") {
            then("it should throw a RuntimeException") {
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error"
                coEvery {
                    firestoreService.getDocumentsFromSubcollection(any(), any(), any(), eq(TestData::class.java))
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.getAll(parentId1, parentId2)
                }
                exception.message shouldContain "Failed to get all entities from subcollection '$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }
    }

    given("GetById") {
        `when`("getById is called with an existing id") {
            then("it should return the entity") {
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

                val result = testRepository.getById(entityId, parentId1, parentId2)

                result shouldBe expectedEntity
            }
        }

        `when`("getById is called with a blank id") {
            then("it should return null") {
                val result = testRepository.getById("", parentId1, parentId2)
                result.shouldBeNull()
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
        }

        `when`("firestoreService returns null") {
            then("getById should return null") {
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

                val result = testRepository.getById(entityId, parentId1, parentId2)

                result.shouldBeNull()
            }
        }

        `when`("firestoreService fails during getById") {
            then("it should throw a RuntimeException") {
                val entityId = "testId"
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error"
                coEvery {
                    firestoreService.getDocumentFromSubcollection(any(), any(), any(), any(), eq(TestData::class.java))
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.getById(entityId, parentId1, parentId2)
                }
                exception.message shouldContain "Failed to get entity '$entityId' from subcollection '$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }
    }

    given("Delete") {
        `when`("delete is called with a valid id") {
            then("it should call deleteDocumentFromSubcollection") {
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

                testRepository.delete(entityId, parentId1, parentId2)

                coVerify {
                    firestoreService.deleteDocumentFromSubcollection(
                        parentCollection = expectedParentPath,
                        parentId = parentId2,
                        subcollection = testSubcollectionName,
                        documentId = entityId
                    )
                }
            }
        }

        `when`("delete is called with a blank id") {
            then("it should throw an IllegalArgumentException") {
                val exception = shouldThrow<IllegalArgumentException> {
                    testRepository.delete("", parentId1, parentId2)
                }
                exception.message shouldBe "Document ID cannot be blank for deletion."
                coVerify(exactly = 0) { firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any()) }
            }
        }

        `when`("firestoreService fails during delete") {
            then("it should throw a RuntimeException") {
                val entityId = "testId"
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error"
                coEvery {
                    firestoreService.deleteDocumentFromSubcollection(any(), any(), any(), any())
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.delete(entityId, parentId1, parentId2)
                }
                exception.message shouldContain "Failed to delete entity '$entityId' from subcollection '$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }
    }
    
    context("isEntityIdBlank default implementation") {
        `when`("called with blank strings") {
            then("should return true") {
                 spyTestRepository.isEntityIdBlank("").shouldBeTrue()
                 spyTestRepository.isEntityIdBlank("   ").shouldBeTrue()
            }
        }
        `when`("called with non-blank string") {
            then("should return false") {
                spyTestRepository.isEntityIdBlank("id").shouldBeFalse()
            }
        }
    }
})
