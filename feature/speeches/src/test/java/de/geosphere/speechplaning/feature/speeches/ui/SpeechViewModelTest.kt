package de.geosphere.speechplaning.feature.speeches.ui

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.UserRole
import de.geosphere.speechplaning.data.authentication.SpeechPermissionPolicy
import de.geosphere.speechplaning.data.usecases.speeches.DeleteSpeechUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.speeches.SaveSpeechUseCase
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
class SpeechViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()

    // Mocks
    lateinit var getSpeechesUseCase: GetSpeechesUseCase
    lateinit var saveSpeechUseCase: SaveSpeechUseCase
    lateinit var deleteSpeechUseCase: DeleteSpeechUseCase
    lateinit var observeCurrentUserUseCase: ObserveCurrentUserUseCase
    lateinit var permissionPolicy: SpeechPermissionPolicy
    lateinit var viewModel: SpeechViewModel

    // Test Data
    val dummySpeech = Speech(id = "1", number = "10", subject = "Test Speech")
    val dummyUser = AppUser(uid = "uid1", email = "test@test.com", displayName = "Tester", role = UserRole.ADMIN)

    beforeTest {
        Dispatchers.setMain(testDispatcher)

        getSpeechesUseCase = mockk()
        saveSpeechUseCase = mockk()
        deleteSpeechUseCase = mockk()
        observeCurrentUserUseCase = mockk()
        permissionPolicy = mockk()

        every { getSpeechesUseCase() } returns MutableStateFlow(Result.success(listOf(dummySpeech)))
        every { observeCurrentUserUseCase() } returns MutableStateFlow(dummyUser)

        every { permissionPolicy.canCreate(any()) } returns true
        every { permissionPolicy.canManageGeneral(any()) } returns true
        every { permissionPolicy.canEdit(any(), any()) } returns true
        every { permissionPolicy.canDelete(any(), any()) } returns true

        viewModel = SpeechViewModel(
            getSpeechesUseCase,
            saveSpeechUseCase,
            deleteSpeechUseCase,
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
                    val successState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                    successState.speeches.size shouldBe 1
                    successState.speeches.first() shouldBe dummySpeech
                    successState.canCreateSpeech.shouldBeTrue()
                    successState.canEditSpeech.shouldBeTrue()
                    successState.canDeleteSpeech.shouldBeTrue()
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    Given("Permission checks reflect in UI State") {
        When("User has limited permissions") {
            every { permissionPolicy.canCreate(any()) } returns false
            every { permissionPolicy.canManageGeneral(any()) } returns false

            val restrictedViewModel = SpeechViewModel(
                getSpeechesUseCase,
                saveSpeechUseCase,
                deleteSpeechUseCase,
                observeCurrentUserUseCase,
                permissionPolicy
            )

            Then("UI state flags should be false") {
                runTest {
                    restrictedViewModel.uiState.test {
                        val successState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        successState.canCreateSpeech.shouldBeFalse()
                        successState.canEditSpeech.shouldBeFalse()
                        successState.canDeleteSpeech.shouldBeFalse()
                    }
                }
            }
        }
    }

    Given("Selection Logic") {
        When("selectSpeech is called") {
            Then("selectedSpeech in state should be set") {
                runTest {
                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.selectSpeech(dummySpeech)
                        val updatedState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        updatedState.selectedSpeech shouldBe dummySpeech
                    }
                }
            }
        }

        When("clearSelection is called") {
            Then("selectedSpeech should be null") {
                runTest {
                    viewModel.selectSpeech(dummySpeech)
                    viewModel.uiState.test {
                        awaitItem() // State with selection
                        viewModel.clearSelection()
                        val finalState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        finalState.selectedSpeech.shouldBeNull()
                    }
                }
            }
        }
    }

    Given("Save Speech Logic") {

        When("User has permission and saves a NEW speech") {
            Then("it should call saveUseCase and clear selection") {
                runTest {
                    val newSpeech = Speech(id = "", number = "20", subject = "New")
                    every { permissionPolicy.canCreate(dummyUser) } returns true

                    coEvery { saveSpeechUseCase(newSpeech) } coAnswers {
                        delay(10)
                        Result.success(Unit)
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveSpeech(newSpeech)

                        val inProgressState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        finalState.isActionInProgress.shouldBeFalse()
                        finalState.selectedSpeech.shouldBeNull()
                    }
                    coVerify(exactly = 1) { saveSpeechUseCase(newSpeech) }
                }
            }
        }

        When("User has permission and updates an EXISTING speech") {
            Then("it should call saveUseCase and clear selection") {
                runTest {
                    val existingSpeech = Speech(id = "existing_123", number = "20", subject = "Update")
                    every { permissionPolicy.canEdit(dummyUser, existingSpeech) } returns true

                    coEvery { saveSpeechUseCase(existingSpeech) } coAnswers {
                        delay(10)
                        Result.success(Unit)
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveSpeech(existingSpeech)

                        val inProgressState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        finalState.isActionInProgress.shouldBeFalse()
                    }
                    coVerify(exactly = 1) { saveSpeechUseCase(existingSpeech) }
                }
            }
        }

        // TEST FÜR onFailure beim SPEICHERN
        When("Save speech FAILS (e.g. database error)") {
            Then("it should set error message in state and reset progress") {
                runTest {
                    val newSpeech = Speech(id = "", number = "20", subject = "New")
                    every { permissionPolicy.canCreate(dummyUser) } returns true
                    val errorMessage = "Datenbankfehler"

                    coEvery { saveSpeechUseCase(newSpeech) } coAnswers {
                        delay(10)
                        Result.failure(Exception(errorMessage))
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveSpeech(newSpeech)

                        // 1. Loading wird angezeigt
                        val inProgressState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        // 2. Fehler wird angezeigt
                        val errorState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        errorState.isActionInProgress.shouldBeFalse()
                        errorState.actionError shouldBe errorMessage
                    }
                    coVerify(exactly = 1) { saveSpeechUseCase(newSpeech) }
                }
            }
        }

        When("User is NOT logged in (null)") {
            Then("it should set error and NOT call saveUseCase") {
                runTest {
                    val newSpeech = Speech(id = "", number = "20", subject = "New")
                    // Mock: ObserveCurrentUserUseCase liefert null
                    every { observeCurrentUserUseCase() } returns MutableStateFlow(null)

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveSpeech(newSpeech)

                        val errorState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        errorState.actionError shouldBe "Keine Berechtigung!"
                    }
                    coVerify(exactly = 0) { saveSpeechUseCase(any()) }
                }
            }
        }

        When("User has NO permission to CREATE new speech") {
            Then("it should set error and NOT call saveUseCase") {
                runTest {
                    val newSpeech = Speech(id = "", number = "20", subject = "New")
                    every { permissionPolicy.canCreate(dummyUser) } returns false

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveSpeech(newSpeech)

                        val errorState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        errorState.actionError shouldBe "Keine Berechtigung!"
                    }
                    coVerify(exactly = 0) { saveSpeechUseCase(any()) }
                }
            }
        }

        When("User has NO permission to EDIT existing speech") {
            Then("it should set error and NOT call saveUseCase") {
                runTest {
                    val existingSpeech = Speech(id = "existing_123", number = "20", subject = "Update")
                    every { permissionPolicy.canEdit(dummyUser, existingSpeech) } returns false

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.saveSpeech(existingSpeech)

                        val errorState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        errorState.actionError shouldBe "Keine Berechtigung!"
                    }
                    coVerify(exactly = 0) { saveSpeechUseCase(any()) }
                }
            }
        }
    }

    Given("Delete Speech Logic") {
        When("User has permission and deletes existing speech") {
            Then("it should call deleteUseCase and clear selection") {
                runTest {
                    val idToDelete = dummySpeech.id
                    coEvery { deleteSpeechUseCase(idToDelete) } coAnswers {
                        delay(10)
                        Result.success(Unit)
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.deleteSpeech(idToDelete)

                        val inProgressState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        val finalState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        finalState.isActionInProgress.shouldBeFalse()
                        finalState.selectedSpeech.shouldBeNull()
                    }

                    coVerify(exactly = 1) { deleteSpeechUseCase(idToDelete) }
                }
            }
        }

        // TEST FÜR onFailure beim LÖSCHEN
        When("Delete speech FAILS (e.g. network error)") {
            Then("it should set error message in state") {
                runTest {
                    val idToDelete = dummySpeech.id
                    val errorMessage = "Löschen fehlgeschlagen"

                    coEvery { deleteSpeechUseCase(idToDelete) } coAnswers {
                        delay(10)
                        Result.failure(Exception(errorMessage))
                    }

                    viewModel.uiState.test {
                        awaitItem() // Initial Success
                        viewModel.deleteSpeech(idToDelete)

                        // 1. Loading
                        val inProgressState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        inProgressState.isActionInProgress.shouldBeTrue()

                        // 2. Fehler
                        val errorState = awaitItem().shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
                        errorState.isActionInProgress.shouldBeFalse()
                        errorState.actionError shouldBe errorMessage
                    }
                    coVerify(exactly = 1) { deleteSpeechUseCase(idToDelete) }
                }
            }
        }
    }
})
