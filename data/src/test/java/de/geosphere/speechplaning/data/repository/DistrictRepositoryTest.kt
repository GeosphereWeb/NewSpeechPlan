package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.services.ICollectionActions
import de.geosphere.speechplaning.data.repository.services.IFlowActions
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class DistrictRepositoryTest : BehaviorSpec({

    lateinit var collectionActions: ICollectionActions
    lateinit var flowActions: IFlowActions
    lateinit var districtRepository: DistrictRepository

    beforeEach {
        collectionActions = mockk(relaxed = true)
        flowActions = mockk(relaxed = true)
        districtRepository = DistrictRepository(collectionActions, flowActions)
    }

    given("getActiveDistricts") {
        `when`("query is successful") {
            then("it should return active districts from collection") {
                val activeDistrict = District(
                    id = "active1",
                    circuitOverseerId = "1",
                    name = "Max Mustermann",
                    active = true
                )
                val inactiveDistrict = District(
                    id = "inactive1",
                    circuitOverseerId = "2",
                    name = "Inactive District",
                    active = false
                )

                coEvery { collectionActions.getDocuments("districts", District::class.java) } returns listOf(
                    activeDistrict,
                    inactiveDistrict
                )

                val result = districtRepository.getActiveDistricts()

                result.size shouldBe 1
                result[0] shouldBe activeDistrict
                coVerify { collectionActions.getDocuments("districts", District::class.java) }
            }
        }

        `when`("collectionActions fails") {
            then("it should throw a runtime exception") {
                val simulatedException = RuntimeException("Simulated Firestore error")

                coEvery { collectionActions.getDocuments("districts", District::class.java) } throws simulatedException

                val exception = shouldThrow<RuntimeException> {
                    districtRepository.getActiveDistricts()
                }

                exception.message shouldBe "Failed to get active districts from districts"
                exception.cause shouldBe simulatedException
                coVerify { collectionActions.getDocuments("districts", District::class.java) }
            }
        }
    }

    given("getAllDistrict") {
        `when`("query is successful") {
            then("it should return all districts from collection") {
                val district1 = District(
                    id = "district1",
                    circuitOverseerId = "1",
                    name = "District 1",
                    active = true
                )
                val district2 = District(
                    id = "district2",
                    circuitOverseerId = "2",
                    name = "District 2",
                    active = false
                )

                coEvery { collectionActions.getDocuments("districts", District::class.java) } returns listOf(
                    district1,
                    district2
                )

                val result = districtRepository.getAllDistrict()

                result.size shouldBe 2
                result shouldBe listOf(district1, district2)
                coVerify { collectionActions.getDocuments("districts", District::class.java) }
            }
        }

        `when`("collectionActions fails") {
            then("it should throw a runtime exception") {
                val simulatedException = RuntimeException("Simulated Firestore error")

                coEvery { collectionActions.getDocuments("districts", District::class.java) } throws simulatedException

                val exception = shouldThrow<RuntimeException> {
                    districtRepository.getAllDistrict()
                }

                exception.message shouldBe "Failed to get all districts from districts"
                exception.cause shouldBe simulatedException
                coVerify { collectionActions.getDocuments("districts", District::class.java) }
            }
        }
    }

    given("saveDistrict") {
        `when`("saving a district") {
            then("it should call setDocument and return the district id") {
                val district = District(
                    id = "district1",
                    circuitOverseerId = "1",
                    name = "Test District",
                    active = true
                )

                coEvery { collectionActions.setDocument("districts", district.id, district) } returns Unit

                val resultId = districtRepository.saveDistrict(district)

                resultId shouldBe district.id
                coVerify {
                    collectionActions.setDocument("districts", district.id, district)
                }
            }
        }
    }

    given("deleteDistrict") {
        `when`("deleting a district") {
            then("it should call deleteDocument") {
                val districtId = "district1"

                coEvery { collectionActions.deleteDocument("districts", districtId) } returns Unit

                districtRepository.deleteDistrict(districtId)

                coVerify {
                    collectionActions.deleteDocument("districts", districtId)
                }
            }
        }
    }
})
