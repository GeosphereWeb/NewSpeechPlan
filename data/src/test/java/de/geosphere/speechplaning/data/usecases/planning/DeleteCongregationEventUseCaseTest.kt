package de.geosphere.speechplaning.data.usecases.planning

import de.geosphere.speechplaning.data.repository.CongregationEventRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class DeleteCongregationEventUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationEventRepository
    lateinit var useCase: DeleteCongregationEventUseCase

    beforeTest {
        repository = mockk()
        useCase = DeleteCongregationEventUseCase(repository)
    }

    given("a request to delete a congregation event") {
        val districtId = "district1"
        val congregationId = "congregation1"
        val eventId = "event1"

        `when`("the repository deletes the event successfully") {
            then("it should return success") {
                coEvery { repository.deleteEvent(districtId, congregationId, eventId) } returns Unit

                val result = useCase(districtId, congregationId, eventId)

                result.shouldBeSuccess()
                coVerify(exactly = 1) { repository.deleteEvent(districtId, congregationId, eventId) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Test exception")
                coEvery { repository.deleteEvent(districtId, congregationId, eventId) } throws exception

                val result = useCase(districtId, congregationId, eventId)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.deleteEvent(districtId, congregationId, eventId) }
            }
        }
    }
})
