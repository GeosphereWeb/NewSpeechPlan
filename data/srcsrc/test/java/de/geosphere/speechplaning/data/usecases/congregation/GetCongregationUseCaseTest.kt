package de.geosphere.speechplaning.data.usecases.congregation

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.data.repository.CongregationRepositoryImpl
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeSortedWith
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class GetCongregationUseCaseTest : BehaviorSpec({

    lateinit var repository: CongregationRepositoryImpl
    lateinit var getCongregationUseCase: GetCongregationUseCase

    beforeTest {
        repository = mockk()
        getCongregationUseCase = GetCongregationUseCase(repository)
    }

    given("a request to get congregations") {
        val unsortedList = listOf(
            Congregation(id = "10", name = "Congregation C"),
            Congregation(id = "2", name = "Congregation A"),
            Congregation(id = "5", name = "Congregation B")
        )
        val exception = RuntimeException("Database error")

        `when`("the repository returns a list of congregations") {
            coEvery { repository.getAllFlow() } returns flowOf(unsortedList)
            val expectedSortedList = unsortedList.sortedBy { it.id.toInt() }

            then("it should emit a success result with the list sorted by id") {
                getCongregationUseCase().test {
                    awaitItem().shouldBeSuccess { congregations ->
                        congregations shouldBe expectedSortedList
                        congregations.shouldBeSortedWith(compareBy { it.id.toInt() })
                    }
                    awaitComplete()
                }
            }
        }

        `when`("the repository returns an empty list") {
            coEvery { repository.getAllFlow() } returns flowOf(emptyList())

            then("it should emit a success result with an empty list") {
                getCongregationUseCase().test {
                    awaitItem().shouldBeSuccess {
                        it.isEmpty() shouldBe true
                    }
                    awaitComplete()
                }
            }
        }

        `when`("the repository flow throws an exception") {
            coEvery { repository.getAllFlow() } returns flow { throw exception }

            then("it should emit a failure result containing the exception") {
                getCongregationUseCase().test {
                    awaitItem().shouldBeFailure {
                        it shouldBe exception
                    }
                    awaitComplete()
                }
            }
        }
    }
})
