package de.geosphere.speechplaning.data.repository.authentication

import com.google.android.gms.tasks.Task
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
        authRepository = AuthRepositoryImpl(firebaseAuth, userRepository, testScope)
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

                // Mock für getIdToken(true)
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

                // Mock für getIdToken(true)
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
            then("it should call firebaseAuth.signOut") {
                authRepository.signOut()
                verify { firebaseAuth.signOut() }
            }
        }
    }

    given("signInWithEmailAndPassword function") {
        `when`("the function is called with email and password") {
            then("it should call the corresponding Firebase function and await the result") {
                // GIVEN
                val email = "test@example.com"
                val password = "password"
                val mockTask = mockk<Task<AuthResult>>()
                val mockAuthResult = mockk<AuthResult>()

                // Mock the Firebase call to return our mock Task
                every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns mockTask

                // Mock the await() extension function for our mock Task
                coEvery { mockTask.await() } returns mockAuthResult

                // WHEN
                testScope.testScheduler.runCurrent()
                authRepository.signInWithEmailAndPassword(email, password)
                testScope.advanceUntilIdle()

                // THEN
                // Verify that the original Firebase function was called.
                verify { firebaseAuth.signInWithEmailAndPassword(email, password) }
                // Verify that await() was called on the task.
                coVerify { mockTask.await() }
            }
        }
    }

    given("createUserWithEmailAndPassword function") {
        `when`("a new user is created") {
            then("a Firebase user and an App user should be created") {
                // GIVEN
                val email = "new@example.com"
                val password = "new_password"
                val mockTask = mockk<Task<AuthResult>>()
                val mockAuthResult = mockk<AuthResult>()
                val mockFirebaseUser = mockk<FirebaseUser>(relaxed = true)
                val mockAppUser = mockk<AppUser>()

                // Mock the Firebase call to return our mock Task
                every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns mockTask
                // Mock the await() extension function for our mock Task
                coEvery { mockTask.await() } returns mockAuthResult
                // Mock the result of the task
                every { mockAuthResult.user } returns mockFirebaseUser
                // Mock the user repository call
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns mockAppUser

                // WHEN
                authRepository.createUserWithEmailAndPassword(email, password)
                testScope.advanceUntilIdle()

                // THEN
                verify { firebaseAuth.createUserWithEmailAndPassword(email, password) }
                coVerify { mockTask.await() }
                coVerify { userRepository.getOrCreateUser(mockFirebaseUser) }
            }
        }
    }
})
