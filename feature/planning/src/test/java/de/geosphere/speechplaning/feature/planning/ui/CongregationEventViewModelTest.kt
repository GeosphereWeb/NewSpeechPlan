package de.geosphere.speechplaning.feature.planning.ui

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.data.usecases.planning.DeleteCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.planning.GetCongregationEventsUseCase
import de.geosphere.speechplaning.data.usecases.planning.SaveCongregationEventUseCase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.time.LocalDate

@ExperimentalCoroutinesApi
class CongregationEventViewModelTest : FunSpec() {

    private val getCongregationEventsUseCase: GetCongregationEventsUseCase = mockk()
    private val saveCongregationEventUseCase: SaveCongregationEventUseCase = mockk()
    private val deleteCongregationEventUseCase: DeleteCongregationEventUseCase = mockk()

    private lateinit var viewModel: CongregationEventViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    init {
        beforeTest {
            Dispatchers.setMain(testDispatcher)
            viewModel = CongregationEventViewModel(
                getCongregationEventsUseCase,
                saveCongregationEventUseCase,
                deleteCongregationEventUseCase
            )
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("loadEvents should update state with events on success") {
            runTest(testDispatcher) {
                val events = listOf(
                    CongregationEvent(
                        id = "1",
                        congregationId = "congregation1",
                        date = LocalDate.now(),
                        eventType = Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER
                    )
                )
                coEvery { getCongregationEventsUseCase(any(), any()) } returns Result.success(events)

                viewModel.loadEvents("district1", "congregation1")

                val uiState = viewModel.uiState.value
                uiState.isLoading shouldBe false
                uiState.events shouldBe events
                uiState.error shouldBe null
            }
        }

        test("loadEvents should update state with error on failure") {
            runTest(testDispatcher) {
                val errorMessage = "Error loading events"
                coEvery { getCongregationEventsUseCase(any(), any()) } returns Result.failure(Exception(errorMessage))

                viewModel.loadEvents("district1", "congregation1")

                val uiState = viewModel.uiState.value
                uiState.isLoading shouldBe false
                uiState.error shouldBe errorMessage
            }
        }

        test("selectEvent should update selectedEvent in state") {
            runTest(testDispatcher) {
                val event =
                    CongregationEvent(
                        id = "1",
                        congregationId = "congregation1",
                        date = LocalDate.now(),
                        eventType = Event.SPECIAL_LECTURE
                    )
                viewModel.selectEvent(event)
                viewModel.uiState.value.selectedEvent shouldBe event
            }
        }

        test("clearSelection should set selectedEvent in state to null") {
            runTest(testDispatcher) {
                val event =
                    CongregationEvent(
                        id = "1",
                        congregationId = "congregation1",
                        date = LocalDate.now(),
                        eventType = Event.SPECIAL_LECTURE
                    )
                viewModel.selectEvent(event)
                viewModel.clearSelection()
                viewModel.uiState.value.selectedEvent shouldBe null
            }
        }

        test("saveEvent should reload events on success") {
            runTest(testDispatcher) {
                val event =
                    CongregationEvent(
                        id = "1",
                        congregationId = "congregation1",
                        date = LocalDate.now(),
                        eventType = Event.MEMORIAL
                    )
                coEvery { saveCongregationEventUseCase(any(), any(), any()) } returns Result.success("1")
                coEvery { getCongregationEventsUseCase(any(), any()) } returns Result.success(listOf(event))

                viewModel.saveEvent("district1", "congregation1", event)

                coVerify { saveCongregationEventUseCase("district1", "congregation1", event) }
                coVerify { getCongregationEventsUseCase("district1", "congregation1") }

                val uiState = viewModel.uiState.value
                uiState.isLoading shouldBe false
                uiState.events shouldBe listOf(event)
            }
        }

        test("saveEvent should update state with error on failure") {
            runTest(testDispatcher) {
                val event =
                    CongregationEvent(
                        id = "1",
                        congregationId = "congregation1",
                        date = LocalDate.now(),
                        eventType = Event.MEMORIAL
                    )
                val errorMessage = "Error saving event"
                coEvery { saveCongregationEventUseCase(any(), any(), any()) } returns
                    Result.failure(Exception(errorMessage))

                viewModel.saveEvent("district1", "congregation1", event)

                val uiState = viewModel.uiState.value
                uiState.isLoading shouldBe false
                uiState.error shouldBe errorMessage
            }
        }

        test("deleteEvent should reload events on success") {
            runTest(testDispatcher) {
                val eventId = "1"
                coEvery { deleteCongregationEventUseCase(any(), any(), any()) } returns
                    Result.success(Unit)
                coEvery { getCongregationEventsUseCase(any(), any()) } returns
                    Result.success(emptyList())

                viewModel.deleteEvent("district1", "congregation1", eventId)

                coVerify { deleteCongregationEventUseCase("district1", "congregation1", eventId) }
                coVerify { getCongregationEventsUseCase("district1", "congregation1") }

                val uiState = viewModel.uiState.value
                uiState.isLoading shouldBe false
                uiState.events shouldBe emptyList()
            }
        }

        test("deleteEvent should update state with error on failure") {
            runTest(testDispatcher) {
                val eventId = "1"
                val errorMessage = "Error deleting event"
                coEvery { deleteCongregationEventUseCase(any(), any(), any()) } returns
                    Result.failure(Exception(errorMessage))

                viewModel.deleteEvent("district1", "congregation1", eventId)

                val uiState = viewModel.uiState.value
                uiState.isLoading shouldBe false
                uiState.error shouldBe errorMessage
            }
        }
    }
}
