package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.data.repository.SpeechRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class DeleteSpeechUseCaseTest : BehaviorSpec({

    val repository = mockk<SpeechRepository>(relaxed = true)
    val useCase = DeleteSpeechUseCase(repository)

    Given("A valid speech ID") {
        val validId = "speech-123"

        When("deleting the speech") {
            coEvery { repository.deleteSpeech(validId) } returns Unit

            val result = useCase(validId)

            Then("it should return success") {
                result.shouldBeSuccess()
            }

            Then("it should call the repository delete method") {
                coVerify(exactly = 1) { repository.deleteSpeech(validId) }
            }
        }
    }

    Given("A blank speech ID") {
        val invalidId = "   "

        When("deleting the speech") {
            val result = useCase(invalidId)

            Then("it should return a failure with IllegalArgumentException") {
                result.shouldBeFailure()
                result.exceptionOrNull() shouldBe IllegalArgumentException("Speech ID cannot be blank.")
            }

            Then("it should NOT call the repository") {
                coVerify(exactly = 0) { repository.deleteSpeech(any()) }
            }
        }
    }

    Given("A repository failure") {
        val validId = "speech-123"
        val errorMessage = "Delete failed"

        When("deleting fails in repository") {
            coEvery { repository.deleteSpeech(validId) } throws RuntimeException(errorMessage)

            val result = useCase(validId)

            Then("it should return a failure result") {
                result.shouldBeFailure()
                result.exceptionOrNull()?.message shouldBe errorMessage
            }
        }
    }
})
