package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SaveSpeakerUseCaseTest : BehaviorSpec({

    lateinit var repository: SpeakerRepositoryImpl
    lateinit var useCase: SaveSpeakerUseCase

    beforeTest {
        repository = mockk()
        useCase = SaveSpeakerUseCase(repository)
    }

    given("a request to save a speaker") {
        val districtId = "district1"
        val congregationId = "congregation1"
        val speakerToSave = Speaker(nameFirst = "Jane", nameLast = "Doe")
        val newSpeakerId = "new_speaker_123"

        `when`("the repository saves the speaker successfully") {
            then("it should return a success result with the new speaker ID") {
                coEvery { repository.saveSpeaker(districtId, congregationId, speakerToSave) } returns newSpeakerId

                val result = useCase(districtId, congregationId, speakerToSave)

                result.shouldBeSuccess(newSpeakerId)
                coVerify(exactly = 1) { repository.saveSpeaker(districtId, congregationId, speakerToSave) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return a failure result") {
                val exception = RuntimeException("Database error")
                coEvery { repository.saveSpeaker(districtId, congregationId, speakerToSave) } throws exception

                val result = useCase(districtId, congregationId, speakerToSave)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.saveSpeaker(districtId, congregationId, speakerToSave) }
            }
        }
    }
})
