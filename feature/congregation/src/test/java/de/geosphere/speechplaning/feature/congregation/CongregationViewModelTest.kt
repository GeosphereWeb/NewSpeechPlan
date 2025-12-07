package de.geosphere.speechplaning.feature.congregation

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.data.UserRole
import de.geosphere.speechplaning.data.authentication.permission.CongregationPermissionPolicy
import de.geosphere.speechplaning.data.usecases.congregation.DeleteCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregation.GetCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregation.SaveCongregationUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class CongregationViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()

    // Mocks
    lateinit var getCongregationUseCase: GetCongregationUseCase
    lateinit var saveCongregationUseCase: SaveCongregationUseCase
    lateinit var deleteCongregationUseCase: DeleteCongregationUseCase
    lateinit var observeCurrentUserUseCase: ObserveCurrentUserUseCase
    lateinit var permissionPolicy: CongregationPermissionPolicy
    lateinit var viewModel: CongregationViewModel

    // Test Data
    val dummyCongregation = Congregation(id = "1", name = "Test Congregation")
    val dummyUser = AppUser(uid = "uid1", email = "test@test.com", displayName = "Tester", role = UserRole.ADMIN)

    beforeTest {
        Dispatchers.setMain(testDispatcher)

        getCongregationUseCase = mockk()
        saveCongregationUseCase = mockk()
        deleteCongregationUseCase = mockk()
        observeCurrentUserUseCase = mockk()
        permissionPolicy = mockk()

        every { getCongregationUseCase() } returns flowOf(Result.success(listOf(dummyCongregation)))
        every { observeCurrentUserUseCase() } returns MutableStateFlow(dummyUser)

        every { permissionPolicy.canCreate(any()) } returns true
        every { permissionPolicy.canManageGeneral(any()) } returns true
        every { permissionPolicy.canEdit(any(), any()) } returns true
        every { permissionPolicy.canDelete(any(), any()) } returns true

        viewModel = CongregationViewModel(
            getCongregationUseCase,
            saveCongregationUseCase,
            deleteCongregationUseCase,
            observeCurrentUserUseCase,
            permissionPolicy
        )
    }

    afterTest {
        Dispatchers.resetMain()
    }

    Given("Initialized ViewModel") {
        Then("UiState should be Success with correct initial data") {
            runTest {
                viewModel.uiState.test {
                    val successState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                    successState.congregations.size shouldBe 1
                    successState.congregations.first() shouldBe dummyCongregation
                    successState.canCreateCongregation.shouldBeTrue()
                    successState.canEditCongregation.shouldBeTrue()
                    successState.canDeleteCongregation.shouldBeTrue()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    Given("Permission checks reflect in UI State") {
        When("User has limited permissions") {
            every { permissionPolicy.canCreate(any()) } returns false
            every { permissionPolicy.canManageGeneral(any()) } returns false

            // Re-create ViewModel to apply new permission mocks
            val restrictedViewModel = CongregationViewModel(
                getCongregationUseCase,
                saveCongregationUseCase,
                deleteCongregationUseCase,
                observeCurrentUserUseCase,
                permissionPolicy
            )

            Then("UI state flags should be false") {
                runTest {
                    restrictedViewModel.uiState.test {
                        val successState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        successState.canCreateCongregation.shouldBeFalse()
                        successState.canEditCongregation.shouldBeFalse()
                        successState.canDeleteCongregation.shouldBeFalse()
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }

    Given("Selection Logic") {
        When("selectCongregation is called") {
            Then("selectedCongregation in state should be set") {
                runTest {
                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.selectCongregation(dummyCongregation)
                        val updatedState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        updatedState.selectedCongregation shouldBe dummyCongregation
                    }
                }
            }
        }

        When("clearSelection is called") {
            Then("selectedCongregation should be null") {
                runTest {
                    viewModel.selectCongregation(dummyCongregation)
                    viewModel.uiState.test {
                        awaitItem() // State with selection
                        viewModel.clearSelection()
                        val finalState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        finalState.selectedCongregation.shouldBeNull()
                    }
                }
            }
        }
    }

    Given("Save Congregation Logic") {
        val newCongregation = Congregation(id = "", name = "New")

        When("User has permission and saves a NEW congregation") {
            Then("it should call saveUseCase and clear selection") {
                runTest {
                    every { permissionPolicy.canCreate(dummyUser) } returns true
                    coEvery { saveCongregationUseCase(newCongregation) } coAnswers {
                        delay(10)
                        Result.success(Unit)
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveCongregation(newCongregation)

                        val inProgressState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        finalState.isActionInProgress.shouldBeFalse()
                        finalState.selectedCongregation.shouldBeNull()
                    }
                    coVerify(exactly = 1) { saveCongregationUseCase(newCongregation) }
                }
            }
        }

        When("Save congregation FAILS") {
            Then("it should set error message in state and reset progress") {
                runTest {
                    val newCongregation = Congregation(id = "", name = "New")
                    val errorMessage = "Datenbankfehler"
                    every { permissionPolicy.canCreate(dummyUser) } returns true

                    coEvery { saveCongregationUseCase(newCongregation) } coAnswers {
                        delay(10)
                        Result.failure(Exception(errorMessage))
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial
                        viewModel.saveCongregation(newCongregation)

                        val inProgressState =
                            awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val errorState =
                            awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        errorState.isActionInProgress.shouldBeFalse()
                        errorState.actionError shouldBe errorMessage
                    }
                }
            }
        }

        When("User has NO permission to CREATE new congregation") {
            Then("it should set error and NOT call saveUseCase") {
                runTest {
                    every { permissionPolicy.canCreate(dummyUser) } returns false

                    viewModel.uiState.test {
                        awaitItem() // Initial
                        viewModel.saveCongregation(newCongregation)
                        val errorState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        errorState.actionError.shouldNotBeBlank()
                    }
                    coVerify(exactly = 0) { saveCongregationUseCase(any()) }
                }
            }
        }
    }

    Given("Delete Congregation Logic") {
        When("User has permission and deletes existing congregation") {
            Then("it should call deleteUseCase") {
                runTest {
                    val idToDelete = dummyCongregation.id
                    coEvery { deleteCongregationUseCase(idToDelete, congregationToDelete.district) } coAnswers {
                        delay(10)
                        Result.success(Unit)
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.deleteCongregation(idToDelete)

                        val inProgressState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                        finalState.isActionInProgress.shouldBeFalse()
                        finalState.selectedCongregation.shouldBeNull()
                    }

                    coVerify(exactly = 1) {
                        deleteCongregationUseCase(
                            idToDelete,
                            congregationToDelete.district
                        )
                    }
                }
            }

            When("User has NO permission to DELETE") {
                Then("it should set error and NOT call deleteUseCase") {
                    runTest {
                        every { permissionPolicy.canDelete(dummyUser, dummyCongregation) } returns false

                        viewModel.uiState.test {
                            awaitItem() // Initial
                            viewModel.deleteCongregation(dummyCongregation.id)
                            val errorState = awaitItem().shouldBeInstanceOf<CongregationUiState.SuccessUIState>()
                            errorState.actionError.shouldNotBeBlank()
                        }
                        coVerify(exactly = 0) {
                            deleteCongregationUseCase(
                                any(),
                                congregationToDelete.district
                            )
                        }
                    }
                }
            }
        }
    }
})
