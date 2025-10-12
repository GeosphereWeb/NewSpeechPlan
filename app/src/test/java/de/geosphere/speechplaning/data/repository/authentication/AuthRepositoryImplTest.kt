package de.geosphere.speechplaning.data.repository.authentication

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.UserProfileChangeRequest
import de.geosphere.speechplaning.data.model.AppUser
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest : BehaviorSpec({

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var userRepository: UserRepository
    lateinit var context: Context

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    lateinit var authRepository: AuthRepository
    val listenerSlot = slot<FirebaseAuth.AuthStateListener>()

    beforeEach {
        mockkStatic(
            "kotlinx.coroutines.tasks.TasksKt",
            "androidx.credentials.CredentialManager"
        )
        mockkConstructor(UserProfileChangeRequest.Builder::class)

        Dispatchers.setMain(testDispatcher)
        firebaseAuth = mockk(relaxed = true) {
            every { addAuthStateListener(capture(listenerSlot)) } returns Unit
        }
        userRepository = mockk(relaxed = true)
        context = mockk(relaxed = true) // Context mocken
        authRepository = AuthRepositoryImpl(firebaseAuth, userRepository, testScope, context)
    }

    afterEach {
        Dispatchers.resetMain()
        unmockkStatic(
            "kotlinx.coroutines.tasks.TasksKt",
            "androidx.credentials.CredentialManager"
        )
        unmockkConstructor(UserProfileChangeRequest.Builder::class)
    }

    given("AuthStateListener") {
        `when`("a Firebase user is signed out (user is null)") {
            then("the state should be Unauthenticated") {
                every { firebaseAuth.currentUser } returns null
                listenerSlot.captured.onAuthStateChanged(firebaseAuth)
                testDispatcher.scheduler.runCurrent()
                testScope.advanceUntilIdle()
                authRepository.authUiState.value shouldBe AuthUiState.Unauthenticated
            }
        }

        `when`("a signed-in user is approved") {
            then("the state should be Authenticated") {
                val firebaseUser = mockk<FirebaseUser>(relaxed = true)
                val approvedAppUser = mockk<AppUser> { every { approved } returns true }
                val mockGetIdTokenTask = mockk<Task<GetTokenResult>>()
                every { firebaseUser.getIdToken(true) } returns mockGetIdTokenTask
                coEvery { mockGetIdTokenTask.await() } returns mockk()
                every { firebaseAuth.currentUser } returns firebaseUser
                coEvery { userRepository.getOrCreateUser(firebaseUser) } returns approvedAppUser

                listenerSlot.captured.onAuthStateChanged(firebaseAuth)
                testDispatcher.scheduler.runCurrent()
                testScope.advanceUntilIdle()

                authRepository.authUiState.value shouldBe AuthUiState.Authenticated(firebaseUser)
            }
        }

        `when`("a signed-in user is not yet approved") {
            then("the state should be NeedsApproval") {
                val firebaseUser = mockk<FirebaseUser>(relaxed = true)
                val notApprovedAppUser = mockk<AppUser> { every { approved } returns false }
                val mockGetIdTokenTask = mockk<Task<GetTokenResult>>()
                every { firebaseUser.getIdToken(true) } returns mockGetIdTokenTask
                coEvery { mockGetIdTokenTask.await() } returns mockk()
                every { firebaseAuth.currentUser } returns firebaseUser
                coEvery { userRepository.getOrCreateUser(firebaseUser) } returns notApprovedAppUser

                listenerSlot.captured.onAuthStateChanged(firebaseAuth)
                testDispatcher.scheduler.runCurrent()
                testScope.advanceUntilIdle()

                authRepository.authUiState.value shouldBe AuthUiState.NeedsApproval
            }
        }
    }

    given("signInWithEmailAndPassword function") {
        `when`("the function is called with email and password") {
            then("it should call the corresponding Firebase function and await the result") {
                val email = "test@example.com"
                val password = "password"
                val mockTask = mockk<Task<AuthResult>>()
                val mockAuthResult = mockk<AuthResult>()

                every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns mockTask
                coEvery { mockTask.await() } returns mockAuthResult

                testScope.launch {
                    authRepository.signInWithEmailAndPassword(email, password)
                }
                testScope.advanceUntilIdle()

                verify { firebaseAuth.signInWithEmailAndPassword(email, password) }
                coVerify { mockTask.await() }
            }
        }
    }
})
