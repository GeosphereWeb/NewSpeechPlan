package de.geosphere.speechplaning.data.usecases.speeches

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.repository.SpeechRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class GetSpeechesUseCaseTest : BehaviorSpec({

    val repository = mockk<SpeechRepository>()
    val useCase = GetSpeechesUseCase(repository)

    Given("A list of speeches in the repository") {
        // Wir nutzen Nummern, die als Int parsbar sind, da der UseCase .toInt() aufruft
        val speech1 = Speech(id = "1", number = "10", subject = "Thema 10", active = true)
        val speech2 = Speech(id = "2", number = "1", subject = "Thema 1", active = true)
        val speech3 = Speech(id = "3", number = "5", subject = "Thema 5", active = true)

        // Unsortierte Liste vom Repo
        val unsortedList = listOf(speech1, speech2, speech3)

        every { repository.getAllSpeechesFlow() } returns flowOf(unsortedList)

        When("getting speeches via use case") {
            Then("it should emit a success result with the list sorted by number") {
                useCase().test {
                    val result = awaitItem()

                    result.shouldBeSuccess()

                    val list = result.getOrThrow()
                    list.size shouldBe 3

                    // Erwartete Reihenfolge: 1, 5, 10
                    list[0] shouldBe speech2 // Nr 1
                    list[1] shouldBe speech3 // Nr 5
                    list[2] shouldBe speech1 // Nr 10

                    awaitComplete()
                }
                verify(exactly = 1) { repository.getAllSpeechesFlow() }
            }
        }
    }

    @Suppress("TooGenericExceptionThrown")
    Given("A repository that throws an error in the flow") {
        val errorMessage = "Database connection failed"
        every { repository.getAllSpeechesFlow() } returns flow {
            throw RuntimeException(errorMessage)
        }

        When("getting speeches") {
            Then("it should catch the error and emit a failure result") {
                useCase().test {
                    val result = awaitItem()

                    result.shouldBeFailure()
                    result.exceptionOrNull()?.message shouldBe errorMessage

                    awaitComplete()
                }
            }
        }
    }

    Given("Speeches with non-integer number format") {
        // IMPORTANT: List must have > 1 element to trigger sortedBy selector execution!
        val badSpeech1 = Speech(id = "4", number = "nan", subject = "Bad Number")
        val goodSpeech = Speech(id = "5", number = "1", subject = "Good Number")

        every { repository.getAllSpeechesFlow() } returns flowOf(listOf(badSpeech1, goodSpeech))

        When("getting speeches") {
            Then("it should catch the parsing error and emit failure") {
                useCase().test {
                    val result = awaitItem()

                    result.shouldBeFailure()
                    result.exceptionOrNull().shouldBeInstanceOf<NumberFormatException>()

                    awaitComplete()
                }
            }
        }
    }
})
