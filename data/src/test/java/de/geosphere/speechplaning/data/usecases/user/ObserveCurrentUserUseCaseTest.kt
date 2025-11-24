package de.geosphere.speechplaning.data.usecases.user

import app.cash.turbine.test
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.data.UserRole
import de.geosphere.speechplaning.data.authentication.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class ObserveCurrentUserUseCaseTest : BehaviorSpec({

    val userRepository = mockk<UserRepository>()
    val observeCurrentUserUseCase = ObserveCurrentUserUseCase(userRepository)

    Given("A user repository returning a specific user") {
        val testUser = AppUser(
            uid = "uid1",
            email = "test@test.com",
            displayName = "Test User",
            role = UserRole.NONE
        )
        every { userRepository.currentUser } returns flowOf(testUser)

        When("invoke is called") {
            Then("it should emit the same user") {
                observeCurrentUserUseCase().test {
                    awaitItem() shouldBe testUser
                    awaitComplete()
                }
            }
        }
    }

    Given("A user repository returning null (no user logged in)") {
        every { userRepository.currentUser } returns flowOf(null)

        When("invoke is called") {
            Then("it should emit null") {
                observeCurrentUserUseCase().test {
                    awaitItem() shouldBe null
                    awaitComplete()
                }
            }
        }
    }
})
