package de.geosphere.speechplaning.feature.speaker.ui

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.authentication.permission.SpeakerPermissionPolicy
import de.geosphere.speechplaning.data.usecases.congregation.ObserveAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.speaker.DeleteSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speaker.SaveSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class SpeakerViewModelTest : BehaviorSpec({

    val testDispatcher = UnconfinedTestDispatcher()

    lateinit var getSpeakersUseCase: GetSpeakersUseCase
    lateinit var saveSpeakerUseCase: SaveSpeakerUseCase
    lateinit var deleteSpeakerUseCase: DeleteSpeakerUseCase
    lateinit var observeAllCongregationsUseCase: ObserveAllCongregationsUseCase
    lateinit var getSpeechesUseCase: GetSpeechesUseCase
    lateinit var observeCurrentUserUseCase: ObserveCurrentUserUseCase
    lateinit var permissionPolicy: SpeakerPermissionPolicy
    lateinit var viewModel: SpeakerViewModel

    beforeSpec {
        Dispatchers.setMain(testDispatcher)
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    beforeTest {
        getSpeakersUseCase = mockk()
        saveSpeakerUseCase = mockk()
        deleteSpeakerUseCase = mockk()
        observeAllCongregationsUseCase = mockk()
        getSpeechesUseCase = mockk()
        observeCurrentUserUseCase = mockk()
        permissionPolicy = mockk()
    }

    given("a SpeakerViewModel") {
        `when`("it is initialized") {
            then("it should load speakers successfully") {
                runTest(testDispatcher) {
                    val speakers = listOf(Speaker(id = "1", firstName = "John"))
                    coEvery { getSpeakersUseCase.invoke() } returns flowOf(Result.success(speakers))
                    coEvery { observeAllCongregationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { getSpeechesUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { observeCurrentUserUseCase.invoke() } returns flowOf(null)

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase,
                        saveSpeakerUseCase,
                        deleteSpeakerUseCase,
                        observeAllCongregationsUseCase,
                        getSpeechesUseCase,
                        observeCurrentUserUseCase,
                        permissionPolicy
                    )

                    viewModel.uiState.test {
                        val state = awaitItem()
                        (state as? SpeakerUiState.SuccessUIState).let {
                            requireNotNull(it) { "Expected SuccessUIState but got ${state::class.simpleName}" }
                            it.speakers shouldBe speakers
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }

            then("it should handle loading failure") {
                runTest(testDispatcher) {
                    val error = RuntimeException("Network error")
                    coEvery { getSpeakersUseCase.invoke() } returns flowOf(Result.failure(error))
                    coEvery { observeAllCongregationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { getSpeechesUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { observeCurrentUserUseCase.invoke() } returns flowOf(null)

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase,
                        saveSpeakerUseCase,
                        deleteSpeakerUseCase,
                        observeAllCongregationsUseCase,
                        getSpeechesUseCase,
                        observeCurrentUserUseCase,
                        permissionPolicy
                    )

                    viewModel.uiState.test {
                        val state = awaitItem()
                        (state as? SpeakerUiState.ErrorUIState).let {
                            requireNotNull(it) { "Expected ErrorUIState but got ${state::class.simpleName}" }
                            it.message shouldBe (error.message ?: "Unknown error")
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        `when`("saveSpeaker is called") {
            then("it should save the speaker") {
                runTest(testDispatcher) {
                    val speakerToSave =
                        Speaker(id = "2", firstName = "Jane", districtId = "dummy", congregationId = "c1")
                    val speakers = listOf(speakerToSave)

                    coEvery { getSpeakersUseCase.invoke() } returns flowOf(Result.success(speakers))
                    coEvery { observeAllCongregationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { getSpeechesUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { observeCurrentUserUseCase.invoke() } returns flowOf(null)
                    coEvery { saveSpeakerUseCase.invoke(speakerToSave) } returns Result.success(Unit)
                    coEvery { permissionPolicy.canEdit(any(), any()) } returns true

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase,
                        saveSpeakerUseCase,
                        deleteSpeakerUseCase,
                        observeAllCongregationsUseCase,
                        getSpeechesUseCase,
                        observeCurrentUserUseCase,
                        permissionPolicy
                    )
                    viewModel.uiState.test {
                        val state = awaitItem()
                        (state as? SpeakerUiState.SuccessUIState).let {
                            requireNotNull(it) { "Expected SuccessUIState but got ${state::class.simpleName}" }
                            it.speakers shouldBe speakers
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }

        `when`("deleteSpeaker is called") {
            then("it should delete the speaker") {
                runTest(testDispatcher) {
                    val speakers =
                        listOf(Speaker(id = "1", firstName = "John", districtId = "dummy", congregationId = "c1"))

                    coEvery { getSpeakersUseCase.invoke() } returns flowOf(Result.success(speakers))
                    coEvery { observeAllCongregationsUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { getSpeechesUseCase.invoke() } returns flowOf(Result.success(emptyList()))
                    coEvery { observeCurrentUserUseCase.invoke() } returns flowOf(null)
                    coEvery { deleteSpeakerUseCase.invoke("dummy", "c1", "1") } returns Result.success(Unit)
                    coEvery { permissionPolicy.canDelete(any(), any()) } returns true

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase,
                        saveSpeakerUseCase,
                        deleteSpeakerUseCase,
                        observeAllCongregationsUseCase,
                        getSpeechesUseCase,
                        observeCurrentUserUseCase,
                        permissionPolicy
                    )

                    viewModel.uiState.test {
                        val state = awaitItem()
                        (state as? SpeakerUiState.SuccessUIState).let {
                            requireNotNull(it) { "Expected SuccessUIState but got ${state::class.simpleName}" }
                            it.speakers shouldBe speakers
                        }
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }
})
