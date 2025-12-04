package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.data.repository.SpeakerRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class DeleteSpeakerUseCaseTest : BehaviorSpec({

    lateinit var repository: SpeakerRepositoryImpl
    lateinit var useCase: DeleteSpeakerUseCase

    beforeTest {
        repository = mockk()
        useCase = DeleteSpeakerUseCase(repository)
    }

    given("a request to delete a speaker") {
        val districtId = "district1"
        val congregationId = "congregation1"
        val speakerId = "speaker1"

        `when`("the repository deletes the speaker successfully") {
            then("it should return success") {
                coEvery { repository.deleteSpeaker(districtId, congregationId, speakerId) } returns Unit

                val result = useCase(districtId, congregationId, speakerId)

                result.shouldBeSuccess()
                coVerify(exactly = 1) { repository.deleteSpeaker(districtId, congregationId, speakerId) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return failure") {
                val exception = RuntimeException("Database error")
                coEvery { repository.deleteSpeaker(districtId, congregationId, speakerId) } throws exception

                val result = useCase(districtId, congregationId, speakerId)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.deleteSpeaker(districtId, congregationId, speakerId) }
            }
        }
    }
})
