package de.geosphere.speechplaning.ui.login

import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.geosphere.speechplaning.data.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.repository.authentication.AuthUiState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class AuthViewModelTest : BehaviorSpec({

    lateinit var authRepository: AuthRepository
    lateinit var viewModel: AuthViewModel
    val testDispatcher = StandardTestDispatcher()

    beforeEach {
        Dispatchers.setMain(testDispatcher)
        // Mock the authUiState to return a controllable StateFlow
        authRepository = mockk(relaxed = true)
        coEvery { authRepository.authUiState } returns MutableStateFlow(AuthUiState.Unauthenticated)
        viewModel = AuthViewModel(authRepository)
    }

    afterEach {
        Dispatchers.resetMain()
    }

    given("createUserWithEmailAndPassword") {
        `when`("successful") {
            then("updates state to success") {
                // Given
                val email = "test@example.com"
                val password = "password"
                coEvery { authRepository.createUserWithEmailAndPassword(email, password) } returns Unit

                // When
                viewModel.createUserWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.loginActionUiState.value
                state.isSuccess.shouldBeTrue()
                coVerify { authRepository.createUserWithEmailAndPassword(email, password) }
            }
        }
        `when`("failure with collision exception") {
            then("updates state with error") {
                // Given
                val email = "test@example.com"
                val password = "password"
                val collisionException = mockk<FirebaseAuthUserCollisionException>()
                coEvery { authRepository.createUserWithEmailAndPassword(email, password) } throws collisionException

                // When
                viewModel.createUserWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.loginActionUiState.value
                state.error.shouldNotBeNull()
                state.error!! shouldContain "existiert bereits"
            }
        }
        `when`("generic exception") {
            then("posts error") {
                // Given
                val email = "test@example.com"
                val password = "password"
                val errorMessage = "Ein unerwarteter Fehler ist aufgetreten"
                coEvery { authRepository.createUserWithEmailAndPassword(email, password) } throws
                    Exception(errorMessage)

                // When
                viewModel.createUserWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.loginActionUiState.value
                state.error.shouldNotBeNull()
                state.error!! shouldContain errorMessage
            }
        }
    }
    given("signInWithEmailAndPassword") {
        `when`("successful") {
            then("updates state to success") {
                // Given
                val email = "test@example.com"
                val password = "password"
                coEvery { authRepository.signInWithEmailAndPassword(email, password) } returns Unit

                // When
                viewModel.signInWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.loginActionUiState.value
                state.isSuccess.shouldBeTrue()
                coVerify { authRepository.signInWithEmailAndPassword(email, password) }
            }
        }
        `when`("failure") {
            then("updates state with error") {
                // Given
                val email = "test@example.com"
                val password = "password"
                val errorMessage = "Anmeldung fehlgeschlagen"
                coEvery { authRepository.signInWithEmailAndPassword(email, password) } throws
                    Exception(errorMessage)

                // When
                viewModel.signInWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.loginActionUiState.value
                state.error.shouldNotBeNull()
                state.error!! shouldContain errorMessage
            }
        }
    }

    given("the viewmodel") {
        `when`("signOut is called") {
            then("it should call the repository's signOut method") {
                viewModel.signOut()
                verify { authRepository.signOut() }
            }
        }

        `when`("checkUserStatusAgain is called") {
            then("it calls repository forceReloadAndCheckUserStatus") {
                viewModel.checkUserStatusAgain()
                testDispatcher.scheduler.advanceUntilIdle()
                coVerify { authRepository.forceReloadAndCheckUserStatus() }
            }
        }
    }

    given("the login action ui state contains an error") {
        `when`("resetActionState is called") {
            then("the state should be reset to the default") {
                // GIVEN: an error state
                coEvery { authRepository.signInWithEmailAndPassword(any(), any()) } throws
                    Exception("Failed")
                viewModel.signInWithEmailAndPassword("a", "b")
                testDispatcher.scheduler.advanceUntilIdle()

                // WHEN: the state is reset
                viewModel.resetActionState()

                // THEN: the state should be the default initial state
                val expectedState = LoginActionUiState()
                viewModel.loginActionUiState.value shouldBe expectedState
            }
        }
    }
})
