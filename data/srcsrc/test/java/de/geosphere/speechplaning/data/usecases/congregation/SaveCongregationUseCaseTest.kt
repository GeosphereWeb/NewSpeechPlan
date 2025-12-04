package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SaveCongregationUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationRepositoryImpl
    lateinit var saveCongregationUseCase: SaveCongregationUseCase

    beforeTest {
        repository = mockk()
        saveCongregationUseCase = SaveCongregationUseCase(repository)
    }

    given("a request to save a congregation") {
        val validCongregation = Congregation(id = "1", name = "Test Congregation")
        val invalidCongregation = Congregation(id = " ", name = "Invalid Congregation")
        val exception = RuntimeException("Database error")

        `when`("the congregation is valid and the repository operation succeeds") {
            coEvery { repository.save(validCongregation) } returns Unit

            val result = saveCongregationUseCase(validCongregation)

            then("it should return a success result") {
                result.shouldBeSuccess()
            }
            then("it should call the repository's save method exactly once") {
                coVerify(exactly = 1) { repository.save(validCongregation) }
            }
        }

        `when`("the congregation id is blank") {
            val result = saveCongregationUseCase(invalidCongregation)

            then("it should return a failure result with an IllegalArgumentException") {
                result.shouldBeFailure {
                    it.shouldBeInstanceOf<IllegalArgumentException>()
                }
            }
            then("it should not call the repository's save method") {
                coVerify(exactly = 0) { repository.save(any()) }
            }
        }

        `when`("the repository throws an exception") {
            coEvery { repository.save(validCongregation) } throws exception

            val result = saveCongregationUseCase(validCongregation)

            then("it should return a failure result with the thrown exception") {
                result.shouldBeFailure {
                    it shouldBe exception
                }
            }
            then("it should call the repository's save method exactly once") {
                coVerify(exactly = 1) { repository.save(validCongregation) }
            }
        }
    }
})
