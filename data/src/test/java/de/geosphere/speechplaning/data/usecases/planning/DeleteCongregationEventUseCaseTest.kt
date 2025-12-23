package de.geosphere.speechplaning.data.usecases.planning

import de.geosphere.speechplaning.data.repository.CongregationEventRepositoryImpl
import de.geosphere.speechplaning.data.usecases.congregationEvent.DeleteCongregationEventUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

class DeleteCongregationEventUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationEventRepositoryImpl
    lateinit var useCase: DeleteCongregationEventUseCase

    beforeTest {
        repository = mockk()
        useCase = DeleteCongregationEventUseCase(repository)
    }

    given("a request to delete a congregation event") {
        val eventId = "event1"

        `when`("the repository deletes the event successfully") {
            then("it should return success") {
                coEvery { repository.deleteEvent(eventId) } returns Unit

                val result = runBlocking { useCase(eventId) }

                result.shouldBeSuccess()
                coVerify(exactly = 1) { repository.deleteEvent(eventId) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Test exception")
                coEvery { repository.deleteEvent(eventId) } throws exception

                val result = runBlocking { useCase(eventId) }

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.deleteEvent(eventId) }
            }
        }
    }
})
