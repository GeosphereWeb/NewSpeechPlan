package de.geosphere.speechplaning.data.usecases

import de.geosphere.speechplaning.data.authentication.AuthRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk

class SignInWithEmailAndPasswordUseCaseTest : BehaviorSpec({

    lateinit var authRepository: AuthRepository
    lateinit var useCase: SignInWithEmailAndPasswordUseCase // This will be a spyk

    beforeEach {
        authRepository = mockk(relaxed = true)
        // We spyk the real object to allow mocking of its private methods
        useCase = spyk(SignInWithEmailAndPasswordUseCase(authRepository), recordPrivateCalls = true)
    }

    given("a user tries to sign in") {
        val validEmail = "test@example.com"
        val validPassword = "password123"

        `when`("credentials are valid and repository succeeds") {
            then("it should return success and call the repository") {
                // Arrange: Mock the private method to simulate a valid email
                every { useCase["isEmailValid"](validEmail) } returns true

                // Act
                val result = useCase(validEmail, validPassword)

                // Assert
                result.shouldBeSuccess()
                coVerify(exactly = 1) { authRepository.signInWithEmailAndPassword(validEmail, validPassword) }
            }
        }

        `when`("email is invalid") {
            val invalidEmail = "not-an-email"
            then("it should return a failure with an invalid email message") {
                // Arrange: Mock the private method to simulate an invalid email
                every { useCase["isEmailValid"](invalidEmail) } returns false

                // Act
                val result = useCase(invalidEmail, validPassword)

                // Assert
                val exception = result.shouldBeFailure<IllegalArgumentException>()
                exception shouldHaveMessage "Die E-Mail-Adresse ist ungültig."
                coVerify(exactly = 0) { authRepository.signInWithEmailAndPassword(any(), any()) }
            }
        }

        `when`("password is blank") {
            val blankPassword = "    "
            then("it should return a failure with a blank password message") {
                // Arrange: Mock the private method to simulate a valid email
                every { useCase["isEmailValid"](validEmail) } returns true

                // Act
                val result = useCase(validEmail, blankPassword)

                // Assert
                val exception = result.shouldBeFailure<IllegalArgumentException>()
                exception shouldHaveMessage "Das Passwort darf nicht leer sein."
                coVerify(exactly = 0) { authRepository.signInWithEmailAndPassword(any(), any()) }
            }
        }

        `when`("repository throws an exception") {
            then("it should return a failure containing that exception") {
                // Arrange
                every { useCase["isEmailValid"](validEmail) } returns true
                val repositoryException = Exception("Network error")
                coEvery { authRepository.signInWithEmailAndPassword(validEmail, validPassword) } throws
                    repositoryException

                // Act
                val result = useCase(validEmail, validPassword)

                // Assert
                val exception = result.shouldBeFailure()
                exception shouldBe repositoryException

                coVerify(exactly = 1) { authRepository.signInWithEmailAndPassword(validEmail, validPassword) }
            }
        }
    }
})
