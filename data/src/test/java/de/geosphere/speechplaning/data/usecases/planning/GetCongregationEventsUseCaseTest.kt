package de.geosphere.speechplaning.data.usecases.planning

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate

class GetCongregationEventsUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationEventRepositoryImpl

    beforeTest {
        repository = mockk()
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
                coEvery { repository.getAllEventsForCongregation(districtId, congregationId) } returns events

                val useCase = de.geosphere.speechplaning.data.usecases.congregationEvent.GetCongregationEventsUseCase(
                    repository
                )

                val result = useCase(districtId, congregationId)

                result.shouldBeSuccess(events)
                coVerify(exactly = 1) { repository.getAllEventsForCongregation(districtId, congregationId) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Test exception")
                coEvery { repository.getAllEventsForCongregation(districtId, congregationId) } throws exception

                val useCase = de.geosphere.speechplaning.data.usecases.congregationEvent.GetCongregationEventsUseCase(
                    repository
                )

                val result = useCase(districtId, congregationId)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.getAllEventsForCongregation(districtId, congregationId) }
            }
        }
    }
})
