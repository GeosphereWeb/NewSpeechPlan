package de.geosphere.speechplaning.feature.districts.ui

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.DistrictRepositoryImpl
import de.geosphere.speechplaning.data.usecases.districts.SaveDistrictUseCase
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
    lateinit var districtRepository: DistrictRepositoryImpl
    lateinit var saveDistrictUseCase: SaveDistrictUseCase
    lateinit var viewModel: DistrictsViewModel

    // Test Data
    val dummyDistrict = District(id = "1", name = "Test District")

    beforeTest {
        Dispatchers.setMain(testDispatcher)

        districtRepository = mockk()
        saveDistrictUseCase = mockk()

        every { districtRepository.getAllDistrictFlow() } returns MutableStateFlow(
            listOf(
                dummyDistrict
            )
        )

        viewModel = DistrictsViewModel(
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
                viewModel.uiState.test {
                    val successState = awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
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
                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.selectDistrict(dummyDistrict)
                        val updatedState =
                            awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
                        updatedState.selectedDistrict shouldBe dummyDistrict
                    }
                }
            }
        }

        When("clearSelection is called") {
            Then("selectedDistrict should be null") {
                runTest {
                    viewModel.selectDistrict(dummyDistrict)
                    viewModel.uiState.test {
                        awaitItem() // State with selection
                        viewModel.clearSelection()
                        val finalState = awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
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
                        Result.success(Unit)
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveDistrict(newDistrict)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
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

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveDistrict(newDistrict)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val errorState = awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
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
                    coEvery { districtRepository.deleteDistrict(idToDelete) } returns Unit

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.deleteDistrict(idToDelete)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
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

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.deleteDistrict(idToDelete)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val errorState = awaitItem().shouldBeInstanceOf<DistrictsUiState.Success>()
                        errorState.isActionInProgress.shouldBeFalse()
                        errorState.actionError shouldBe errorMessage
                    }
                    coVerify(exactly = 1) { districtRepository.deleteDistrict(idToDelete) }
                }
            }
        }
    }
})
