package de.geosphere.speechplaning.data.usecases.planning

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.usecases.congregationEvent.ObserveAllEventsForCongregationUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

class GetCongregationEventsUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationEventRepository
    lateinit var useCase: ObserveAllEventsForCongregationUseCase

    beforeTest {
        repository = mockk()
        useCase = ObserveAllEventsForCongregationUseCase(repository)
    }

    given("a request to get all congregation events") {
        val districtId = "district1"
        val congregationId = "congregation1"

        `when`("the repository returns a list of events") {
            then("it should return success with the list of events") {
                val events = listOf(
                    CongregationEvent(
                        id = "event1",
                        dateString = LocalDate.now().toString(),
                        eventType = Event.CONVENTION,
                        speakerCongregationId = congregationId
                    ),
                    CongregationEvent(
                        id = "event2",
                        dateString = LocalDate.now().plusDays(1).toString(),
                        eventType = Event.CONVENTION,
                        speakerCongregationId = congregationId
                    )
                )

                coEvery {
                    repository.getAllEventsForCongregation(districtId, congregationId)
                } returns events

                // Mock the getAllFlow to return events as a Flow
                coEvery {
                    repository.getAllFlow(districtId, congregationId)
                } returns flowOf(events)

                useCase(districtId, congregationId).test {
                    val result = awaitItem()
                    result.isSuccess shouldBe true
                    result.getOrNull() shouldBe events

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Test exception")

                coEvery {
                    repository.getAllFlow(districtId, congregationId)
                } returns flow { throw exception }

                useCase(districtId, congregationId).test {
                    val result = awaitItem()
                    result.isFailure shouldBe true
                    result.exceptionOrNull() shouldBe exception

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})
