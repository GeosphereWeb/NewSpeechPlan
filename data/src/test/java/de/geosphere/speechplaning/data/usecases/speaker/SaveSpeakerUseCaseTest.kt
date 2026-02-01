package de.geosphere.speechplaning.data.usecases.speaker

import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.repository.SpeakerRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SaveSpeakerUseCaseTest : BehaviorSpec({

    lateinit var repository: SpeakerRepository
    lateinit var useCase: SaveSpeakerUseCase

    beforeTest {
        repository = mockk()
        useCase = SaveSpeakerUseCase(repository)
    }

    given("a request to save a speaker with embedded location") {
        val districtId = "district1"
        val congregationId = "congregation1"

        `when`("the repository saves the speaker successfully") {
            then("it should return a success result") {
                val speakerToSave = Speaker(
                    firstName = "Jane",
                    lastName = "Doe",
                    districtId = districtId,
                    congregationId = congregationId
                )
                val newSpeakerId = "new_speaker_123"
                coEvery {
                    repository.saveSpeaker(districtId, congregationId, speakerToSave)
                } returns newSpeakerId

                val result = useCase(speakerToSave)

                result.shouldBeSuccess(Unit)
                coVerify(exactly = 1) { repository.saveSpeaker(districtId, congregationId, speakerToSave) }
            }
        }

        `when`("the repository throws an exception") {
            then("it should return a failure result") {
                val speakerToSave = Speaker(
                    firstName = "Jane",
                    lastName = "Doe",
                    districtId = districtId,
                    congregationId = congregationId
                )
                val exception = RuntimeException("Database error")
                coEvery {
                    repository.saveSpeaker(districtId, congregationId, speakerToSave)
                } throws exception

                val result = useCase(speakerToSave)

                result.shouldBeFailure(exception)
                coVerify(exactly = 1) { repository.saveSpeaker(districtId, congregationId, speakerToSave) }
            }
        }
    }

    given("a request to save a speaker with blank name") {
        val districtId = "district1"
        val congregationId = "congregation1"

        `when`("speaker lastName is blank") {
            then("it should return a failure result with validation error") {
                val invalidSpeaker = Speaker(
                    firstName = "Jane",
                    lastName = "",
                    districtId = districtId,
                    congregationId = congregationId
                )

                val result = useCase(invalidSpeaker)

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Speaker name cannot be blank."
                coVerify(exactly = 0) {
                    repository.saveSpeaker(any(), any(), any())
                }
            }
        }
    }

    given("a request to save a speaker with blank location") {
        `when`("districtId or congregationId is blank") {
            then("it should return a failure result with validation error") {
                val speakerWithoutLocation = Speaker(
                    firstName = "Jane",
                    lastName = "Doe",
                    districtId = "",
                    congregationId = ""
                )

                val result = useCase(speakerWithoutLocation)

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldContain "DistrictId and CongregationId must not be blank"
                coVerify(exactly = 0) {
                    repository.saveSpeaker(any(), any(), any())
                }
            }
        }
    }

    given("a request to move a speaker to a different congregation") {
        val oldDistrictId = "district1"
        val oldCongregationId = "congregation1"
        val newDistrictId = "district2"
        val newCongregationId = "congregation2"

        `when`("moving a speaker successfully") {
            then("it should save at new location and delete from old location") {
                val speakerToMove = Speaker(
                    id = "speaker_123",
                    firstName = "Jane",
                    lastName = "Doe",
                    districtId = newDistrictId,
                    congregationId = newCongregationId
                )

                coEvery {
                    repository.saveSpeaker(newDistrictId, newCongregationId, speakerToMove)
                } returns "speaker_123"
                coEvery {
                    repository.deleteSpeaker(
                        districtId = oldDistrictId,
                        congregationId = oldCongregationId,
                        speakerId = speakerToMove.id
                    )
                } returns Unit

                val result = useCase(speakerToMove, oldDistrictId, oldCongregationId)

                result.shouldBeSuccess(Unit)
                coVerify(exactly = 1) {
                    repository.saveSpeaker(newDistrictId, newCongregationId, speakerToMove)
                }
                coVerify(exactly = 1) {
                    repository.deleteSpeaker(
                        districtId = oldDistrictId,
                        congregationId = oldCongregationId,
                        speakerId = speakerToMove.id
                    )
                }
            }
        }

        `when`("attempting to move a speaker without an ID") {
            then("it should fail with validation error") {
                val speakerWithoutId = Speaker(
                    id = "",
                    firstName = "Jane",
                    lastName = "Doe",
                    districtId = newDistrictId,
                    congregationId = newCongregationId
                )

                val result = useCase(speakerWithoutId, oldDistrictId, oldCongregationId)

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Cannot move a speaker without an existing ID."
                coVerify(exactly = 0) {
                    repository.saveSpeaker(any(), any(), any())
                }
            }
        }
    }
})
