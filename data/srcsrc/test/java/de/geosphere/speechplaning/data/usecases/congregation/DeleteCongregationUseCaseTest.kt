package de.geosphere.speechplaning.data.usecases.congregation

import de.geosphere.speechplaning.data.repository.CongregationRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class DeleteCongregationUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationRepositoryImpl
    lateinit var deleteCongregationUseCase: DeleteCongregationUseCase

    beforeTest {
        repository = mockk()
        deleteCongregationUseCase = DeleteCongregationUseCase(repository)
    }

    given("a request to delete a congregation") {
        val validCongregationId = "123"
        val invalidCongregationId = " "
        val exception = RuntimeException("Database error")

        `when`("the congregationId is valid and the repository operation succeeds") {
            coEvery { repository.delete(validCongregationId) } returns Unit

            val result = deleteCongregationUseCase(validCongregationId)

            then("it should return a success result") {
                result.shouldBeSuccess()
            }
            then("it should call the repository's delete method exactly once") {
                coVerify(exactly = 1) { repository.delete(validCongregationId) }
            }
        }

        `when`("the congregationId is blank") {
            val result = deleteCongregationUseCase(invalidCongregationId)

            then("it should return a failure result with an IllegalArgumentException") {
                result.shouldBeFailure {
                    it.shouldBeInstanceOf<IllegalArgumentException>()
                }
            }
            then("it should not call the repository's delete method") {
                coVerify(exactly = 0) { repository.delete(any()) }
            }
        }

        `when`("the repository throws an exception") {
            coEvery { repository.delete(validCongregationId) } throws exception

            val result = deleteCongregationUseCase(validCongregationId)

            then("it should return a failure result with the thrown exception") {
                result.shouldBeFailure {
                    it shouldBe exception
                }
            }
            then("it should call the repository's delete method exactly once") {
                coVerify(exactly = 1) { repository.delete(validCongregationId) }
            }
        }
    }
})
