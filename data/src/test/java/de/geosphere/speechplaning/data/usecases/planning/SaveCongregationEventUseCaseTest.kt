package de.geosphere.speechplaning.data.usecases.planning

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import de.geosphere.speechplaning.data.usecases.congregationEvent.SaveCongregationEventUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class SaveCongregationEventUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationEventRepository
    lateinit var useCase: SaveCongregationEventUseCase

    beforeTest {
        repository = mockk()
        useCase = SaveCongregationEventUseCase(repository)
    }

    given("a request to save a congregation event") {
        val event = CongregationEvent(
            id = "event1",
            dateString = LocalDate.now().toString(),
            eventType = Event.CONVENTION,
            speakerCongregationId = "congregation1"
        )

        `when`("the repository saves the event successfully") {
            then("it should return success") {
                val expectedEventId = "newEventId"
                coEvery { repository.saveEvent(event) } returns expectedEventId

                val result = runBlocking { useCase(event) }
                result.shouldBeSuccess()

                coVerify(exactly = 1) { repository.saveEvent(event) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Test exception")
                coEvery { repository.saveEvent(event) } throws exception

                val result = runBlocking { useCase(event) }

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.saveEvent(event) }
            }
        }
    }
})
