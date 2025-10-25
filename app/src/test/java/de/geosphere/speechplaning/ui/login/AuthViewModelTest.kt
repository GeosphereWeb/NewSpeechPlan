
import android.app.Activity
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.geosphere.speechplaning.data.model.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.model.repository.authentication.AuthUiState
import de.geosphere.speechplaning.data.usecases.CreateUserWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.GoogleSignInUseCase
import de.geosphere.speechplaning.data.usecases.SignOutUseCase
import de.geosphere.speechplaning.ui.login.AuthViewModel
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

// Annahme: AuthActionUiState ist im selben Paket oder global definiert,
// damit der Test darauf zugreifen kann.
// Falls es in AuthViewModel definiert ist, müsste es 'AuthViewModel.AuthActionUiState' sein.
// Ich nehme an, es ist eine Top-Level-Definition.
data class AuthActionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@ExperimentalCoroutinesApi
class AuthViewModelTest : BehaviorSpec({

    // Mocks für alle Abhängigkeiten
    lateinit var createUserWithEmailAndPasswordUseCase: CreateUserWithEmailAndPasswordUseCase
    lateinit var signInWithEmailAndPasswordUseCase: SignInWithEmailAndPasswordUseCase
    lateinit var googleSignInUseCase: GoogleSignInUseCase
    lateinit var signOutUseCase: SignOutUseCase
    lateinit var authRepository: AuthRepository

    lateinit var viewModel: AuthViewModel
    val testDispatcher = StandardTestDispatcher()

    beforeEach {
        Dispatchers.setMain(testDispatcher)

        // Alle Abhängigkeiten mocken
        createUserWithEmailAndPasswordUseCase = mockk()
        signInWithEmailAndPasswordUseCase = mockk()
        googleSignInUseCase = mockk()
        signOutUseCase = mockk()
        authRepository =
            mockk(relaxed = true) // relaxed = true ist nützlich, wenn nicht alle Methoden gemockt werden müssen

        // Grundlegendes Verhalten für den StateFlow mocken
        // Beachte: Der authUiState wird durch den Listener im AuthRepositoryImpl aktualisiert,
        // der wiederum den DetermineAppUserStatusUseCase verwendet.
        // Für diesen ViewModel-Test ist es ausreichend, den Flow direkt zu mocken.
        coEvery { authRepository.authUiState } returns MutableStateFlow(AuthUiState.Unauthenticated)

        // ViewModel mit den Mocks initialisieren
        viewModel = AuthViewModel(
            createUserWithEmailAndPasswordUseCase = createUserWithEmailAndPasswordUseCase,
            signInWithEmailAndPasswordUseCase = signInWithEmailAndPasswordUseCase,
            googleSignInUseCase = googleSignInUseCase,
            signOutUseCase = signOutUseCase,
            authRepository = authRepository
        )
    }

    afterEach {
        Dispatchers.resetMain()
    }

    given("createUserWithEmailAndPassword") {
        val email = "test@example.com"
        val password = "password123"
        val displayName = "Test User"

        `when`("successful") {
            then("updates actionUiState to success") {
                // Given: Der UseCase gibt Erfolg zurück
                coEvery { createUserWithEmailAndPasswordUseCase(email, password, displayName) } returns Result.success(
                    Unit
                )

                // When
                viewModel.createUserWithEmailAndPassword(email, password, displayName)
                testDispatcher.scheduler.advanceUntilIdle() // Coroutine ausführen lassen

                // Then
                val state = viewModel.actionUiState.value
                state.isSuccess.shouldBeTrue()
                state.isLoading.shouldBeFalse()
                state.error.shouldBeNull()
                coVerify { createUserWithEmailAndPasswordUseCase(email, password, displayName) }
            }
        }

        `when`("failure with collision exception") {
            then("updates actionUiState with specific error") {
                // Given: Der UseCase gibt einen Kollisionsfehler zurück
                val collisionException = mockk<FirebaseAuthUserCollisionException>()
                coEvery {
                    createUserWithEmailAndPasswordUseCase(email, password, displayName)
                } returns Result.failure(collisionException)

                // When
                viewModel.createUserWithEmailAndPassword(email, password, displayName)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isSuccess.shouldBeFalse()
                state.isLoading.shouldBeFalse()
                state.error.shouldNotBeNull()
                state.error shouldContain "existiert bereits"
                coVerify { createUserWithEmailAndPasswordUseCase(email, password, displayName) }
            }
        }

        `when`("validation failure") {
            then("updates actionUiState with validation error") {
                // Given: Der UseCase gibt einen Validierungsfehler zurück (z.B. durch IllegalArgumentException)
                val validationMessage = "Das Passwort muss mindestens 6 Zeichen lang sein."
                coEvery {
                    createUserWithEmailAndPasswordUseCase(any(), any(), any())
                } returns Result.failure(IllegalArgumentException(validationMessage))

                // When
                viewModel.createUserWithEmailAndPassword("email@test.de", "123", "name")
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isSuccess.shouldBeFalse()
                state.isLoading.shouldBeFalse()
                state.error shouldBe validationMessage
                coVerify { createUserWithEmailAndPasswordUseCase(any(), any(), any()) }
            }
        }
    }

    given("signInWithEmailAndPassword") {
        val email = "test@example.com"
        val password = "password"

        `when`("successful") {
            then("updates actionUiState to success") {
                // Given
                coEvery { signInWithEmailAndPasswordUseCase(email, password) } returns Result.success(Unit)

                // When
                viewModel.signInWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isSuccess.shouldBeTrue()
                state.isLoading.shouldBeFalse()
                state.error.shouldBeNull()
                coVerify { signInWithEmailAndPasswordUseCase(email, password) }
            }
        }

        `when`("failure with invalid credentials") {
            then("updates actionUiState with specific error") {
                // Given
                val exception = mockk<FirebaseAuthInvalidCredentialsException>()
                coEvery { signInWithEmailAndPasswordUseCase(email, password) } returns Result.failure(exception)

                // When
                viewModel.signInWithEmailAndPassword(email, password)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isSuccess.shouldBeFalse()
                state.isLoading.shouldBeFalse()
                state.error.shouldNotBeNull()
                state.error shouldContain "Passwort ist falsch"
                coVerify { signInWithEmailAndPasswordUseCase(email, password) }
            }
        }
    }

    given("signInWithGoogle") {
        val mockActivity = mockk<Activity>()

        `when`("successful") {
            then("updates actionUiState to success and finishes loading") {
                // Given
                coEvery { googleSignInUseCase(mockActivity) } returns Result.success(Unit)

                // When
                viewModel.signInWithGoogle(mockActivity)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isLoading.shouldBeFalse() // Should finish loading
                state.error.shouldBeNull() // No error
                coVerify { googleSignInUseCase(mockActivity) }
            }
        }

        `when`("failure") {
            then("updates actionUiState with error and finishes loading") {
                // Given
                val errorMessage = "Google Sign-In fehlgeschlagen."
                coEvery { googleSignInUseCase(mockActivity) } returns Result.failure(Exception(errorMessage))

                // When
                viewModel.signInWithGoogle(mockActivity)
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isLoading.shouldBeFalse() // Should finish loading
                state.error shouldBe errorMessage // Error message
                coVerify { googleSignInUseCase(mockActivity) }
            }
        }
    }

    given("the viewmodel") {
        `when`("signOut is called") {
            then("it should call the sign out use case") {
                // Given
                coEvery { signOutUseCase() } returns Result.success(Unit)

                // When
                viewModel.signOut()
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isLoading.shouldBeFalse()
                state.error.shouldBeNull() // Kein Fehler im Erfolgsfall

                coVerify { signOutUseCase() }
            }
        }

        `when`("signOut fails") {
            then("it should update actionUiState with an error") {
                // Given
                val errorMessage = "Logout failed."
                coEvery { signOutUseCase() } returns Result.failure(Exception(errorMessage))

                // When
                viewModel.signOut()
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isLoading.shouldBeFalse()
                state.error shouldContain errorMessage // Fehler im Fehlerfall
                coVerify { signOutUseCase() }
            }
        }


        `when`("checkUserStatusAgain is called") {
            then("it calls repository forceReloadAndCheckUserStatus") {
                // Given (authRepository.forceReloadAndCheckUserStatus() ist relaxed gemockt)
                // When
                viewModel.checkUserStatusAgain()
                testDispatcher.scheduler.advanceUntilIdle()

                // Then
                val state = viewModel.actionUiState.value
                state.isLoading.shouldBeFalse() // Ladezustand sollte zurückgesetzt werden
                coVerify { authRepository.forceReloadAndCheckUserStatus() }
            }
        }
    }

    given("the login action ui state contains an error") {
        `when`("resetActionState is called") {
            then("the state should be reset to the default") {
                // GIVEN: an error state
                coEvery { signInWithEmailAndPasswordUseCase(any(), any()) } returns Result.failure(Exception("Failed"))
                viewModel.signInWithEmailAndPassword("a", "b")
                testDispatcher.scheduler.advanceUntilIdle()
                viewModel.actionUiState.value.error.shouldNotBeNull() // Bestätigen, dass ein Fehler vorhanden ist

                // WHEN: the state is reset
                viewModel.resetActionState()
                testDispatcher.scheduler.advanceUntilIdle()

                // THEN: the state should be the default initial state
                val state = viewModel.actionUiState.value
                state.isLoading.shouldBeFalse()
                state.error.shouldBeNull()
                state.isSuccess.shouldBeFalse()
            }
        }
    }
})
