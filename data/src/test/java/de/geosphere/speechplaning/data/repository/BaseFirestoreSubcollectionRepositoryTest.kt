package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.SavableDataClass
import de.geosphere.speechplaning.data.repository.base.FirestoreSubcollectionRepository
import de.geosphere.speechplaning.data.repository.services.ISubcollectionActions
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

internal data class TestData(val id: String = "", val data: String = "") : SavableDataClass()

internal class TestSubcollectionRepository(
    subcollectionActions: ISubcollectionActions
) : FirestoreSubcollectionRepository<TestData, String>(
    subcollectionActions = subcollectionActions,
    subcollectionName = "testSubcollection",
    clazz = TestData::class.java
) {
    override fun extractIdFromEntity(entity: TestData): String = entity.id
    override fun buildParentCollectionPath(vararg parentIds: String): String = "parentCollection/${parentIds[0]}"
    override fun getParentDocumentId(vararg parentIds: String): String = parentIds[1]
}

internal class BaseFirestoreSubcollectionRepositoryTest : BehaviorSpec({

    lateinit var subcollectionActions: ISubcollectionActions
    lateinit var testRepository: TestSubcollectionRepository

    val testSubcollectionName = "testSubcollection"
    val parentId1 = "parent1"
    val parentId2 = "childParent1"

    beforeEach {
        subcollectionActions = mockk(relaxed = true)
        testRepository = TestSubcollectionRepository(subcollectionActions)
    }

    given("Save") {
        `when`("saving a new entity") {
            then("it should call addDocumentToSubcollection and return new id") {
                val newEntity = TestData(id = "", data = "newData")
                val generatedId = "newGeneratedId"
                val expectedParentPath = "parentCollection/$parentId1"

                coEvery {
                    subcollectionActions.addDocumentToSubcollection(
                        parentCollection = expectedParentPath,
                        parentId = parentId2,
                        subcollection = testSubcollectionName,
                        data = newEntity
                    )
                } returns generatedId

                val resultId = testRepository.save(newEntity, parentId1, parentId2)

                resultId shouldBe generatedId
                coVerify {
                    subcollectionActions.addDocumentToSubcollection(
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
                val existingEntity = TestData(id = "existingId", data = "updatedData")
                val expectedParentPath = "parentCollection/$parentId1"

                coEvery {
                    subcollectionActions.setDocumentInSubcollection(
                        parentCollection = expectedParentPath,
                        parentId = parentId2,
                        subcollection = testSubcollectionName,
                        documentId = existingEntity.id,
                        data = existingEntity
                    )
                } returns Unit

                val resultId = testRepository.save(existingEntity, parentId1, parentId2)

                resultId shouldBe existingEntity.id
                coVerify {
                    subcollectionActions.setDocumentInSubcollection(
                        parentCollection = expectedParentPath,
                        parentId = parentId2,
                        subcollection = testSubcollectionName,
                        documentId = existingEntity.id,
                        data = existingEntity
                    )
                }
            }
        }

        `when`("subcollectionActions fails for a new entity") {
            then("it should throw a RuntimeException") {
                val newEntity = TestData(id = "", data = "someData")
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error on add"

                coEvery {
                    subcollectionActions.addDocumentToSubcollection(any(), any(), any(), any())
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.save(newEntity, parentId1, parentId2)
                }
                exception.message shouldBe "Failed to save entity '[new]' in subcollection " +
                    "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }

        `when`("subcollectionActions fails for an existing entity") {
            then("it should throw a RuntimeException") {
                val existingEntity = TestData(id = "someId", data = "someData")
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error on set"

                coEvery {
                    subcollectionActions.setDocumentInSubcollection(any(), any(), any(), any(), any())
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.save(existingEntity, parentId1, parentId2)
                }
                exception.message shouldBe "Failed to save entity 'someId' in subcollection " +
                    "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }
    }

    given("GetAllFlow") {
        `when`("getAllFlow is called") {
            then("it should throw UnsupportedOperationException") {
                val exception = shouldThrow<UnsupportedOperationException> {
                    testRepository.getAllFlow(parentId1, parentId2)
                }
                exception.message shouldBe "Subcollection flow is not implemented yet."
            }
        }
    }

    given("GetById") {
        `when`("getById is called with an existing id") {
            then("it should return the entity") {
                val entityId = "testId"
                val expectedEntity = TestData(id = entityId, data = "testData")
                val expectedParentPath = "parentCollection/$parentId1"
                coEvery {
                    subcollectionActions.getDocumentFromSubcollection(
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
                result shouldBe null
                coVerify(exactly = 0) {
                    subcollectionActions.getDocumentFromSubcollection(
                        any(), // parentCollectionPath
                        any(), // parentDocumentId
                        any(), // subcollectionName
                        any(), // documentId
                        eq(TestData::class.java) // objectClass
                    )
                }
            }
        }

        `when`("subcollectionActions returns null") {
            then("getById should return null") {
                val entityId = "nonExistentId"
                val expectedParentPath = "parentCollection/$parentId1"
                coEvery {
                    subcollectionActions.getDocumentFromSubcollection(
                        parentCollectionPath = expectedParentPath,
                        parentDocumentId = parentId2,
                        subcollectionName = testSubcollectionName,
                        documentId = entityId,
                        objectClass = TestData::class.java
                    )
                } returns null

                val result = testRepository.getById(entityId, parentId1, parentId2)

                result shouldBe null
            }
        }

        `when`("subcollectionActions fails during getById") {
            then("it should throw a RuntimeException") {
                val entityId = "testId"
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error"
                coEvery {
                    subcollectionActions.getDocumentFromSubcollection(
                        any(), any(), any(), any(),
                        eq(TestData::class.java)
                    )
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.getById(entityId, parentId1, parentId2)
                }
                exception.message shouldBe "Failed to get entity '$entityId' from subcollection " +
                    "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
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
                    subcollectionActions.deleteDocumentFromSubcollection(
                        parentCollection = expectedParentPath,
                        parentId = parentId2,
                        subcollection = testSubcollectionName,
                        documentId = entityId
                    )
                } returns Unit

                testRepository.delete(entityId, parentId1, parentId2)

                coVerify {
                    subcollectionActions.deleteDocumentFromSubcollection(
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
                coVerify(exactly = 0) {
                    subcollectionActions.deleteDocumentFromSubcollection(any(), any(), any(), any())
                }
            }
        }

        `when`("subcollectionActions fails during delete") {
            then("it should throw a RuntimeException") {
                val entityId = "testId"
                val expectedParentPath = "parentCollection/$parentId1"
                val exceptionMessage = "Firestore error"
                coEvery {
                    subcollectionActions.deleteDocumentFromSubcollection(any(), any(), any(), any())
                } throws Exception(exceptionMessage)

                val exception = shouldThrow<RuntimeException> {
                    testRepository.delete(entityId, parentId1, parentId2)
                }
                exception.message shouldBe "Failed to delete entity '$entityId' from subcollection " +
                    "'$testSubcollectionName' under parent '$parentId2' in '$expectedParentPath'"
                exception.cause?.message shouldBe exceptionMessage
            }
        }
    }
})
