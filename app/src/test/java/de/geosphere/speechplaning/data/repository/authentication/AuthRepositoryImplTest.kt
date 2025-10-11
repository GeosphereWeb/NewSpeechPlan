package de.geosphere.speechplaning.data.repository.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import de.geosphere.speechplaning.data.model.AppUser
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
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

    beforeSpec {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    afterSpec {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    beforeEach {
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

    given("signOut function") {
        `when`("signOut is called") {
            then("it should call firebaseAuth.signOut and credentialManager.clearCredentialState") {
                mockkStatic(CredentialManager::class) {
                    val credentialManager = mockk<CredentialManager>(relaxed = true)
                    every { CredentialManager.create(context) } returns credentialManager

                    testScope.launch {
                        authRepository.signOut()
                    }
                    testScope.advanceUntilIdle()

                    verify { firebaseAuth.signOut() }
                    coVerify { credentialManager.clearCredentialState(any()) }
                }
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

    given("createUserWithEmailAndPassword function") {
        `when`("a new user is created") {
            then("a Firebase user and an App user should be created and the profile updated") {
                val email = "new@example.com"
                val password = "new_password"
                val displayName = "New User"
                val mockCreateUserTask = mockk<Task<AuthResult>>()
                val mockAuthResult = mockk<AuthResult>()
                val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)
                val mockAppUser = mockk<AppUser>()

                every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns mockCreateUserTask
                coEvery { mockCreateUserTask.await() } returns mockAuthResult
                every { mockAuthResult.user } returns mockFirebaseUser
                every { mockFirebaseUser.updateProfile(any()) } returns Tasks.forResult(null)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns mockAppUser

                testScope.launch {
                    authRepository.createUserWithEmailAndPassword(email, password, displayName)
                }
                testScope.advanceUntilIdle()

                verify { firebaseAuth.createUserWithEmailAndPassword(email, password) }
                coVerify { mockCreateUserTask.await() }
                verify { mockFirebaseUser.updateProfile(any()) }
                coVerify { userRepository.getOrCreateUser(mockFirebaseUser) }
            }
        }
    }
})
