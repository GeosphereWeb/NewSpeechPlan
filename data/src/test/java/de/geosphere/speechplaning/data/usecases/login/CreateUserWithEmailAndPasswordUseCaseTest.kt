package de.geosphere.speechplaning.data.usecases.login

import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.authentication.AuthRepository
import de.geosphere.speechplaning.data.authentication.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk

class CreateUserWithEmailAndPasswordUseCaseTest : BehaviorSpec({

    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var useCase: CreateUserWithEmailAndPasswordUseCase

    val mockFirebaseUser: FirebaseUser = mockk(relaxed = true)

    beforeEach {
        authRepository = mockk()
        userRepository = mockk(relaxed = true)
        useCase = CreateUserWithEmailAndPasswordUseCase(authRepository, userRepository)
    }

    given("a request to create a user") {
        val validEmail = "test@example.com"
        val validPassword = "password123"
        val validName = "Test User"

        `when`("the name is blank") {
            then("it should return a failure with an IllegalArgumentException") {
                val result = useCase(validEmail, validPassword, " ")

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
                result.exceptionOrNull()?.message shouldBe "Der Name darf nicht leer sein."
            }
        }

        `when`("the email is invalid") {
            then("it should return a failure with an IllegalArgumentException") {
                val result = useCase("invalid-email", validPassword, validName)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
                result.exceptionOrNull()?.message shouldBe "Die E-Mail-Adresse ist ungültig."
            }
        }

        `when`("the password is too short") {
            then("it should return a failure with an IllegalArgumentException") {
                val result = useCase(validEmail, "123", validName)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
                result.exceptionOrNull()?.message shouldStartWith "Das Passwort muss mindestens"
            }
        }

        `when`("all inputs are valid and repositories succeed") {
            then("it should call all repository methods in order and return success") {
                coEvery { authRepository.createUserWithEmailAndPassword(validEmail, validPassword) } returns
                    mockFirebaseUser
                coEvery { authRepository.updateFirebaseUserProfile(mockFirebaseUser, validName) } returns Unit
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns mockk()

                val result = useCase(validEmail, validPassword, validName)

                result.shouldBeSuccess()

                coVerifyOrder {
                    authRepository.createUserWithEmailAndPassword(validEmail, validPassword)
                    authRepository.updateFirebaseUserProfile(mockFirebaseUser, validName)
                    userRepository.getOrCreateUser(mockFirebaseUser)
                }
            }
        }

        `when`("creating the firebase user fails") {
            then("it should return a failure") {
                val errorMessage = "Firebase auth failed"
                coEvery { authRepository.createUserWithEmailAndPassword(any(), any()) } throws Exception(errorMessage)

                val result = useCase(validEmail, validPassword, validName)

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe errorMessage
            }
        }
    }
})
