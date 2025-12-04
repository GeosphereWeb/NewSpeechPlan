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

class SaveCongregationEventUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationEventRepositoryImpl
    lateinit var useCase: SaveCongregationEventUseCase

    beforeTest {
        repository = mockk()
        useCase = SaveCongregationEventUseCase(repository)
    }

    given("a request to save a congregation event") {
        val districtId = "district1"
        val congregationId = "congregation1"
        val event = CongregationEvent(
            id = "event1",
            congregationId = congregationId,
            date = LocalDate.now(),
            eventType = Event.CONVENTION
        )

        `when`("the repository saves the event successfully") {
            then("it should return success with the new event id") {
                val expectedEventId = "newEventId"
                coEvery { repository.saveEvent(districtId, congregationId, event) } returns expectedEventId

                val result = useCase(districtId, congregationId, event)

                result.shouldBeSuccess(expectedEventId)
                coVerify(exactly = 1) { repository.saveEvent(districtId, congregationId, event) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Test exception")
                coEvery { repository.saveEvent(districtId, congregationId, event) } throws exception

                val result = useCase(districtId, congregationId, event)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.saveEvent(districtId, congregationId, event) }
            }
        }
    }
})
