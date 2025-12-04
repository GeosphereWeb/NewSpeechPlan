package de.geosphere.speechplaning.feature.districts.ui

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.core.model.data.UserRole
import de.geosphere.speechplaning.data.authentication.permission.SpeechPermissionPolicy
import de.geosphere.speechplaning.data.usecases.districts.DeleteDistrictUseCase
import de.geosphere.speechplaning.data.usecases.districts.GetDistrictUseCase
import de.geosphere.speechplaning.data.usecases.districts.SaveDistrictUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DistrictsViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()

    // Mocks
    lateinit var getDistrictUseCase: GetDistrictUseCase
    lateinit var deleteDistrictUseCase: DeleteDistrictUseCase
    lateinit var saveDistrictUseCase: SaveDistrictUseCase
    lateinit var observeCurrentUserUseCase: ObserveCurrentUserUseCase
    lateinit var permissionPolicy: SpeechPermissionPolicy
    lateinit var cut: DistrictsViewModel

    // Test Data
    val dummyDistrict = District(id = "1", name = "Test District")
    val dummyUser = AppUser(uid = "uid1", email = "test@test.com", displayName = "Tester", role = UserRole.ADMIN)

    beforeTest {
        Dispatchers.setMain(testDispatcher)

        getDistrictUseCase = mockk()
        deleteDistrictUseCase = mockk()
        saveDistrictUseCase = mockk()
        observeCurrentUserUseCase = mockk()
        permissionPolicy = mockk()

        every { districtRepository.getAllDistrictFlow() } returns MutableStateFlow(
            listOf(
                dummyDistrict
            )
        )

        cut = DistrictsViewModel(
            districtRepository,
            saveDistrictUseCase
        )
    }

    afterTest {
        Dispatchers.resetMain()
    }

    Given("Initialized ViewModel") {
        Then("UiState should be Success with correct initial data") {
            runTest {
                cut.uiState.test {
                    val successState = awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                    successState.districts.size shouldBe 1
                    successState.districts.first() shouldBe dummyDistrict
                    successState.canEditDistrict.shouldBeTrue()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    Given("Selection Logic") {
        When("selectDistrict is called") {
            Then("selectedDistrict in state should be set") {
                runTest {
                    cut.uiState.test {
                        awaitItem() // Initial Success
                        cut.selectDistrict(dummyDistrict)
                        val updatedState =
                            awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        updatedState.selectedDistrict shouldBe dummyDistrict
                    }
                }
            }
        }

        When("clearSelection is called") {
            Then("selectedDistrict should be null") {
                runTest {
                    cut.selectDistrict(dummyDistrict)
                    cut.uiState.test {
                        awaitItem() // State with selection
                        cut.clearSelection()
                        val finalState = awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        finalState.selectedDistrict.shouldBeNull()
                    }
                }
            }
        }
    }

    Given("Save District Logic") {
        When("User saves a NEW district successfully") {
            Then("it should call saveUseCase and clear selection") {
                runTest {
                    val newDistrict = District(id = "", name = "New")
                    coEvery { saveDistrictUseCase(newDistrict) } coAnswers {
                        delay(10)
                        Result.success(Unit)
                    }

                    cut.uiState.test {
                        awaitItem() // Initial Success
                        cut.saveDistrict(newDistrict)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        finalState.isActionInProgress.shouldBeFalse()
                        finalState.selectedDistrict.shouldBeNull()
                    }
                    coVerify(exactly = 1) { saveDistrictUseCase(newDistrict) }
                }
            }
        }

        When("Save district FAILS") {
            Then("it should set error message in state and reset progress") {
                runTest {
                    val newDistrict = District(id = "", name = "New")
                    val errorMessage = "Database error"
                    coEvery { saveDistrictUseCase(newDistrict) } throws Exception(errorMessage)

                    cut.uiState.test {
                        awaitItem() // Initial Success
                        cut.saveDistrict(newDistrict)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val errorState = awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        errorState.isActionInProgress.shouldBeFalse()
                        errorState.actionError shouldBe errorMessage
                    }
                    coVerify(exactly = 1) { saveDistrictUseCase(newDistrict) }
                }
            }
        }
    }

    Given("Delete District Logic") {
        When("User deletes an existing district successfully") {
            Then("it should call repository's delete and clear selection") {
                runTest {
                    val idToDelete = dummyDistrict.id
                    coEvery { districtRepository.deleteDistrict(idToDelete) } coAnswers{
                        delay(10)
                        Result.success(Unit)
                    }

                    cut.uiState.test {
                        awaitItem() // Initial Success
                        cut.deleteDistrict(idToDelete)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        finalState.isActionInProgress.shouldBeFalse()
                        finalState.selectedDistrict.shouldBeNull()
                    }
                    coVerify(exactly = 1) { districtRepository.deleteDistrict(idToDelete) }
                }
            }
        }

        When("Delete district FAILS") {
            Then("it should set error message in state") {
                runTest {
                    val idToDelete = dummyDistrict.id
                    val errorMessage = "Deletion failed"
                    coEvery { districtRepository.deleteDistrict(idToDelete) } throws Exception(
                        errorMessage
                    )

                    cut.uiState.test {
                        awaitItem() // Initial Success
                        cut.deleteDistrict(idToDelete)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val errorState = awaitItem().shouldBeInstanceOf<DistrictUiState.Success>()
                        errorState.isActionInProgress.shouldBeFalse()
                        errorState.actionError shouldBe errorMessage
                    }
                    coVerify(exactly = 1) { districtRepository.deleteDistrict(idToDelete) }
                }
            }
        }
    }
})
