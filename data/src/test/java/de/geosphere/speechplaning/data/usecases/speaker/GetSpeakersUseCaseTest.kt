package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class GetSpeakersUseCaseTest : BehaviorSpec({

    lateinit var repository: SpeakerRepositoryImpl
    lateinit var useCase: GetSpeakersUseCase

    beforeTest {
        repository = mockk()
        useCase = GetSpeakersUseCase(repository)
    }

    given("a request to get all speakers for a congregation") {
        val districtId = "district1"
        val congregationId = "congregation1"

        `when`("the repository returns a list of speakers") {
            then("it should return a success result with the speaker list") {
                val speakers = listOf(Speaker(id = "1", nameFirst = "John"))
                coEvery { repository.getSpeakersForCongregation(districtId, congregationId) } returns speakers

                val result = useCase(districtId, congregationId)

                result.shouldBeSuccess(speakers)
                coVerify(exactly = 1) { repository.getSpeakersForCongregation(districtId, congregationId) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return a failure result") {
                val exception = RuntimeException("Database error")
                coEvery { repository.getSpeakersForCongregation(districtId, congregationId) } throws exception

                val result = useCase(districtId, congregationId)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.getSpeakersForCongregation(districtId, congregationId) }
            }
        }
    }
})
