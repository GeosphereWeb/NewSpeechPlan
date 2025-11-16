package de.geosphere.speechplaning.data.authentication

import android.content.Context
import android.text.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import de.geosphere.speechplaning.data.usecases.login.DetermineAppUserStatusUseCase
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

// Mock für com.google.android.gms.tasks.Task, da es final ist
@Suppress("UNCHECKED_CAST")
private fun <T> mockTask(result: T?, exception: Exception? = null): Task<T> {
    val task: Task<T> = mockk(relaxed = true)
    every { task.isComplete } returns true
    every { task.isCanceled } returns false
    every { task.isSuccessful } returns (exception == null)
    every { task.result } returns result
    every { task.exception } returns exception
    every { task.addOnCompleteListener(any()) } answers {
        val listener = it.invocation.args[0] as OnCompleteListener<T>
        listener.onComplete(task)
        task
    }
    return task
}

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest : BehaviorSpec({

    lateinit var mockFirebaseAuth: FirebaseAuth
    lateinit var determineAppUserStatusUseCase: DetermineAppUserStatusUseCase
    lateinit var testScope: TestScope
    lateinit var mockContext: Context
    lateinit var authRepository: AuthRepositoryImpl
    lateinit var listenerSlot: CapturingSlot<FirebaseAuth.AuthStateListener>

    beforeEach {
        mockFirebaseAuth = mockk(relaxed = true)
        determineAppUserStatusUseCase = mockk()
        testScope = TestScope(UnconfinedTestDispatcher())
        mockContext = mockk(relaxed = true)

        // Capture the listener that is added during the repository's init block
        listenerSlot = slot()
        every { mockFirebaseAuth.addAuthStateListener(capture(listenerSlot)) } returns Unit

        authRepository = AuthRepositoryImpl(
            firebaseAuth = mockFirebaseAuth,
            externalScope = testScope,
            context = mockContext,
            determineAppUserStatusUseCase = determineAppUserStatusUseCase
        )
    }

    afterEach {
        unmockkAll()
    }

    given("Ein neuer Benutzer möchte sich registrieren") {
        `when`("die Registrierung mit E-Mail und Passwort aufgerufen wird") {
            then("sollte Firebase aufgerufen und der neue FirebaseUser zurückgegeben werden") {
                val email = "test@example.com"
                val password = "password123"
                val mockFirebaseUser: FirebaseUser = mockk()
                val mockAuthResult: AuthResult = mockk()
                every { mockAuthResult.user } returns mockFirebaseUser
                coEvery { mockFirebaseAuth.createUserWithEmailAndPassword(email, password) } returns
                    mockTask(mockAuthResult)

                val resultUser = authRepository.createUserWithEmailAndPassword(email, password)

                resultUser shouldBe mockFirebaseUser
                coVerify(exactly = 1) { mockFirebaseAuth.createUserWithEmailAndPassword(email, password) }
            }
        }
    }

    given("Ein existierender Benutzer möchte sich anmelden") {
        `when`("der Login mit E-Mail und Passwort aufgerufen wird") {
            then("sollte die Firebase-Anmeldefunktion aufgerufen werden") {
                val email = "test@example.com"
                val password = "password123"
                val mockAuthResult: AuthResult = mockk(relaxed = true)
                coEvery { mockFirebaseAuth.signInWithEmailAndPassword(email, password) } returns
                    mockTask(mockAuthResult)

                authRepository.signInWithEmailAndPassword(email, password)

                coVerify(exactly = 1) { mockFirebaseAuth.signInWithEmailAndPassword(email, password) }
            }
        }
    }

    given("Profil- und Status-Management") {
        `when`("updateFirebaseUserProfile aufgerufen wird") {
            then("sollte das Profil des Firebase-Benutzers aktualisiert werden") {
                mockkStatic(TextUtils::class)
                every { TextUtils.isEmpty(any()) } answers { firstArg<CharSequence?>().isNullOrEmpty() }

                val mockUser: FirebaseUser = mockk()
                val name = "Test User"
                val profileChangeRequestSlot = slot<UserProfileChangeRequest>()
                every { mockUser.updateProfile(capture(profileChangeRequestSlot)) } returns mockTask(null)

                authRepository.updateFirebaseUserProfile(mockUser, name)

                profileChangeRequestSlot.captured.displayName shouldBe name
                verify(exactly = 1) { mockUser.updateProfile(any()) }
            }
        }

        `when`("forceReloadAndCheckUserStatus aufgerufen wird") {
            then("sollte der UseCase mit forceReload=true getriggert werden") {
                val mockFirebaseUser: FirebaseUser = mockk()
                every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
                coEvery { determineAppUserStatusUseCase.invoke(any(), any(), any()) } returns
                    AuthUiState.Authenticated(mockk())

                authRepository.forceReloadAndCheckUserStatus()
                testScope.testScheduler.advanceUntilIdle()

                coVerify {
                    determineAppUserStatusUseCase.invoke(
                        firebaseUser = mockFirebaseUser,
                        forceReload = true,
                        reloadTokenAction = any()
                    )
                }
            }
        }
    }

    given("Firebase-Interaktionen") {
        `when`("signInWithFirebaseCredential aufgerufen wird") {
            then("sollte die signInWithCredential-Methode von FirebaseAuth aufgerufen werden") {
                val mockCredential: AuthCredential = mockk()
                coEvery { mockFirebaseAuth.signInWithCredential(mockCredential) } returns mockTask(mockk())

                authRepository.signInWithFirebaseCredential(mockCredential)

                coVerify { mockFirebaseAuth.signInWithCredential(mockCredential) }
            }
        }

        `when`("signOutFirebase aufgerufen wird") {
            then("sollte firebaseAuth.signOut() aufgerufen werden") {
                authRepository.signOutFirebase()
                verify(exactly = 1) { mockFirebaseAuth.signOut() }
            }
        }
    }

    given("der AuthStateListener") {
        `when`("der Status sich ändert und ein Benutzer vorhanden ist") {
            then("sollte der determineAppUserStatusUseCase aufgerufen werden") {
                val mockFirebaseUser: FirebaseUser = mockk()
                every { mockFirebaseAuth.currentUser } returns mockFirebaseUser
                coEvery { determineAppUserStatusUseCase.invoke(any(), any(), any()) } returns
                    AuthUiState.Authenticated(mockk())

                // The listener was captured in beforeEach, now we can trigger it
                listenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
                testScope.testScheduler.advanceUntilIdle()

                coVerify(exactly = 1) {
                    determineAppUserStatusUseCase.invoke(
                        firebaseUser = mockFirebaseUser,
                        forceReload = false,
                        reloadTokenAction = any()
                    )
                }
            }
        }
    }
})
