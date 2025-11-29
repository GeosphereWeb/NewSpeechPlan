package de.geosphere.speechplaning.data.usecases.speeches

import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.repository.SpeechRepositoryImpl
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SaveSpeechUseCaseTest : BehaviorSpec({

    // WICHTIG: Sorgt dafür, dass für jeden Testfall (Then) eine neue Instanz der Klasse erstellt wird.
    // Dadurch sind die Mocks (repository) immer frisch und enthalten keine Aufrufe aus vorherigen Tests.
    isolationMode = IsolationMode.InstancePerLeaf

    val repository = mockk<SpeechRepositoryImpl>(relaxed = true)
    val useCase = SaveSpeechUseCase(repository)

    Given("A valid speech") {
        val validSpeech = Speech(id = "123", number = "5", subject = "Ein gutes Thema", active = true)

        When("saving the speech") {
            coEvery { repository.saveSpeech(validSpeech) } returns Unit

            val result = useCase(validSpeech)

            Then("it should return success") {
                result.shouldBeSuccess()
            }

            Then("it should call the repository save method") {
                coVerify(exactly = 1) { repository.saveSpeech(validSpeech) }
            }
        }
    }

    Given("A speech with blank number") {
        val invalidSpeech = Speech(id = "123", number = "   ", subject = "Ein Thema", active = true)

        When("saving the speech") {
            val result = useCase(invalidSpeech)

            Then("it should return a failure with IllegalArgumentException") {
                result.shouldBeFailure()
                val exception = result.exceptionOrNull()

                // Prüfung auf Typ und Nachricht (sicherer als direkter Objekt-Vergleich)
                exception.shouldBeInstanceOf<IllegalArgumentException>()
                exception?.message shouldBe "Speech number and subject cannot be blank."
            }

            Then("it should NOT call the repository") {
                coVerify(exactly = 0) { repository.saveSpeech(any()) }
            }
        }
    }

    Given("A speech with blank subject") {
        val invalidSpeech = Speech(id = "123", number = "5", subject = "", active = true)

        When("saving the speech") {
            val result = useCase(invalidSpeech)

            Then("it should return a failure with IllegalArgumentException") {
                result.shouldBeFailure()
                val exception = result.exceptionOrNull()
                exception.shouldBeInstanceOf<IllegalArgumentException>()
                exception?.message shouldBe "Speech number and subject cannot be blank."
            }
        }
    }

    Given("A repository failure") {
        val validSpeech = Speech(id = "123", number = "5", subject = "Valid", active = true)
        val errorMessage = "Write failed"

        When("saving fails in repository") {
            coEvery { repository.saveSpeech(validSpeech) } throws RuntimeException(errorMessage)

            val result = useCase(validSpeech)

            Then("it should return a failure result") {
                result.shouldBeFailure()
                result.exceptionOrNull()?.message shouldBe errorMessage
            }
        }
    }
})
