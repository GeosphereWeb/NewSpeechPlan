package de.geosphere.speechplaning.feature.speaker.ui

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.usecases.speaker.DeleteSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speaker.SaveSpeakerUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class SpeakerViewModelTest : BehaviorSpec({

    val testDispatcher = StandardTestDispatcher()

    lateinit var getSpeakersUseCase: GetSpeakersUseCase
    lateinit var saveSpeakerUseCase: SaveSpeakerUseCase
    lateinit var deleteSpeakerUseCase: DeleteSpeakerUseCase
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
    }

    val districtId = "test_district"
    val congregationId = "test_congregation"

    given("a SpeakerViewModel") {
        `when`("it is initialized") {
            then("it should load speakers successfully") {
                runTest {
                    val speakers = listOf(Speaker(id = "1", firstName = "John"))
                    coEvery { getSpeakersUseCase(districtId, congregationId) } returns Result.success(speakers)

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase, saveSpeakerUseCase, deleteSpeakerUseCase,
                        districtId, congregationId
                    )

                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe SpeakerUiState(isLoading = false, speakers = speakers)
                    coVerify(exactly = 1) { getSpeakersUseCase(districtId, congregationId) }
                }
            }

            then("it should handle loading failure") {
                runTest {
                    val error = RuntimeException("Network error")
                    coEvery { getSpeakersUseCase(districtId, congregationId) } returns Result.failure(error)

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase, saveSpeakerUseCase, deleteSpeakerUseCase,
                        districtId, congregationId
                    )

                    advanceUntilIdle()

                    viewModel.uiState.value shouldBe SpeakerUiState(isLoading = false, error = error.message)
                }
            }
        }

        `when`("saveSpeaker is called") {
            then("it should save the speaker and reload the list") {
                runTest {
                    val speakerToSave = Speaker(id = "2", firstName = "Jane")
                    coEvery {
                        getSpeakersUseCase(districtId, congregationId)
                    } returns Result.success(emptyList()) andThen Result.success(listOf(speakerToSave))
                    coEvery { saveSpeakerUseCase(districtId, congregationId, speakerToSave) } returns
                        Result.success("2")

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase, saveSpeakerUseCase, deleteSpeakerUseCase,
                        districtId, congregationId
                    )
                    advanceUntilIdle()

                    viewModel.saveSpeaker(speakerToSave)
                    advanceUntilIdle()

                    coVerify(exactly = 1) { saveSpeakerUseCase(districtId, congregationId, speakerToSave) }
                    coVerify(exactly = 2) { getSpeakersUseCase(districtId, congregationId) }
                    viewModel.uiState.value.speakers shouldBe listOf(speakerToSave)
                }
            }
        }

        `when`("deleteSpeaker is called") {
            then("it should delete the speaker and reload the list") {
                runTest {
                    val speakerIdToDelete = "1"
                    val initialSpeakers = listOf(Speaker(id = speakerIdToDelete))
                    coEvery {
                        getSpeakersUseCase(districtId, congregationId)
                    } returns Result.success(initialSpeakers) andThen Result.success(emptyList())
                    coEvery { deleteSpeakerUseCase(districtId, congregationId, speakerIdToDelete) } returns
                        Result.success(Unit)

                    viewModel = SpeakerViewModel(
                        getSpeakersUseCase, saveSpeakerUseCase, deleteSpeakerUseCase,
                        districtId, congregationId
                    )

                    advanceUntilIdle()

                    viewModel.deleteSpeaker(speakerIdToDelete)

                    advanceUntilIdle()

                    coVerify(exactly = 1) { deleteSpeakerUseCase(districtId, congregationId, speakerIdToDelete) }
                    coVerify(exactly = 2) { getSpeakersUseCase(districtId, congregationId) }
                    viewModel.uiState.value.speakers shouldBe emptyList()
                }
            }
        }
    }
})
