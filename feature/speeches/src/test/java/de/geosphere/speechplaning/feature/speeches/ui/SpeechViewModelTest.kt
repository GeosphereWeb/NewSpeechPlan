package de.geosphere.speechplaning.feature.speeches.ui

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechViewModelTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val testDispatcher = UnconfinedTestDispatcher()

    // Mocks
    val getSpeechesUseCase = mockk<GetSpeechesUseCase>()
    val saveSpeechUseCase = mockk<SaveSpeechUseCase>()
    val deleteSpeechUseCase = mockk<DeleteSpeechUseCase>()
    val observeCurrentUserUseCase = mockk<ObserveCurrentUserUseCase>()
    val permissionPolicy = mockk<SpeechPermissionPolicy>()

    lateinit var viewModel: SpeechViewModel

    // Testdaten
    val dummySpeech = Speech(id = "1", number = "10", subject = "Test Speech")
    val dummyUser = AppUser(uid = "uid1", email = "test@test.com", displayName = "Tester", role = UserRole.ADMIN)

    beforeTest {
        Dispatchers.setMain(testDispatcher)

        // Standard-Verhalten für Flows definieren (damit combine initialisiert wird)
        every { getSpeechesUseCase() } returns flowOf(Result.success(listOf(dummySpeech)))
        every { observeCurrentUserUseCase() } returns flowOf(dummyUser)

        // Standard-Policy: Alles erlaubt (kann in Tests überschrieben werden)
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
        Then("UiState should reflect loaded speeches and permissions") {
            val state = viewModel.uiState.value

            state.shouldBeInstanceOf<SpeechUiState.SuccessUIState>()
            val successState = state as SpeechUiState.SuccessUIState

            successState.speeches.size shouldBe 1
            successState.speeches.first() shouldBe dummySpeech
            successState.canCreateSpeech.shouldBeTrue()
            successState.canEditSpeech.shouldBeTrue()
        }
    }

    Given("Selection Logic") {
        When("selectSpeech is called") {
            viewModel.selectSpeech(dummySpeech)

            Then("selectedSpeech in state should be set") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.selectedSpeech shouldBe dummySpeech
            }
        }

        When("clearSelection is called") {
            viewModel.selectSpeech(dummySpeech) // Setup
            viewModel.clearSelection()

            Then("selectedSpeech should be null") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.selectedSpeech.shouldBeNull()
            }
        }
    }

    Given("Save Speech Logic") {

        When("User has permission and saves a new speech") {
            val newSpeech = Speech(id = "", number = "20", subject = "New")

            // Mock Success
            coEvery { saveSpeechUseCase(newSpeech) } returns Result.success(Unit)
            // Für new speech wird canCreate geprüft
            every { permissionPolicy.canCreate(dummyUser) } returns true

            viewModel.saveSpeech(newSpeech)

            Then("it should call saveUseCase") {
                coVerify(exactly = 1) { saveSpeechUseCase(newSpeech) }
            }

            Then("it should clear selection (dialog closed)") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.selectedSpeech.shouldBeNull()
                state.isActionInProgress.shouldBeFalse()
            }
        }

        When("User has NO permission") {
            val newSpeech = Speech(id = "", number = "20", subject = "New")

            // Mock No Permission
            every { permissionPolicy.canCreate(dummyUser) } returns false

            viewModel.saveSpeech(newSpeech)

            Then("it should NOT call saveUseCase and set error") {
                coVerify(exactly = 0) { saveSpeechUseCase(any()) }

                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.actionError shouldBe "Keine Berechtigung!"
            }
        }

        When("Save fails") {
            val speech = Speech(id = "1", number = "10", subject = "Update")
            val errorMsg = "DB Error"

            coEvery { saveSpeechUseCase(speech) } returns Result.failure(RuntimeException(errorMsg))
            every { permissionPolicy.canEdit(dummyUser, speech) } returns true

            viewModel.saveSpeech(speech)

            Then("it should set actionError") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.actionError shouldBe errorMsg
                state.isActionInProgress.shouldBeFalse()
            }
        }
    }

    Given("Delete Speech Logic") {

        When("User has permission and deletes existing speech") {
            val idToDelete = dummySpeech.id

            coEvery { deleteSpeechUseCase(idToDelete) } returns Result.success(Unit)
            every { permissionPolicy.canDelete(dummyUser, dummySpeech) } returns true

            viewModel.deleteSpeech(idToDelete)

            Then("it should call deleteUseCase") {
                coVerify(exactly = 1) { deleteSpeechUseCase(idToDelete) }
            }

            Then("it should clear selection") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.selectedSpeech.shouldBeNull()
            }
        }

        When("User tries to delete speech not in list") {
            viewModel.deleteSpeech("unknown_id")

            Then("it should set error 'Rede nicht gefunden'") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.actionError shouldBe "Rede nicht gefunden."
                coVerify(exactly = 0) { deleteSpeechUseCase(any()) }
            }
        }

        When("User has NO permission to delete") {
            val idToDelete = dummySpeech.id

            every { permissionPolicy.canDelete(dummyUser, dummySpeech) } returns false

            viewModel.deleteSpeech(idToDelete)

            Then("it should set permission error") {
                val state = viewModel.uiState.value as SpeechUiState.SuccessUIState
                state.actionError shouldBe "Keine Berechtigung zum Löschen dieser Rede!"
                coVerify(exactly = 0) { deleteSpeechUseCase(any()) }
            }
        }
    }
})
