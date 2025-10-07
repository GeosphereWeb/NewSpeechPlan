package de.geosphere.speechplaning.data.repository.authentication

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
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle

@ExperimentalCoroutinesApi
class AuthRepositoryImplTest : BehaviorSpec({

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var userRepository: UserRepository

    val testDispatcher = StandardTestDispatcher()
    lateinit var authRepository: AuthRepository

    // Slot to capture the listener
    val listenerSlot = slot<FirebaseAuth.AuthStateListener>()

    beforeEach {
        firebaseAuth = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        // Capture the listener upon initialization of the repository
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } returns Unit
        authRepository = AuthRepositoryImpl(firebaseAuth, userRepository, CoroutineScope(testDispatcher))
    }

    init {
        given("AuthStateListener") {
            `when`("user is null") {
                then("state is Unauthenticated") {
                    // Given
                    every { firebaseAuth.currentUser } returns null

                    // When
                    listenerSlot.captured.onAuthStateChanged(firebaseAuth)
                    advanceUntilIdle()

                    // Then
                    authRepository.authUiState.value shouldBe AuthUiState.Unauthenticated
                }
            }

            `when`("user is approved") {
                then("state is Authenticated") {
                    // Given
                    val firebaseUser = mockk<FirebaseUser>()
                    val approvedAppUser = mockk<AppUser> { every { approved } returns true }
                    val tokenResult = mockk<GetTokenResult>()
                    every { firebaseAuth.currentUser } returns firebaseUser
                    every { firebaseUser.getIdToken(true) } returns Tasks.forResult(tokenResult)
                    coEvery { userRepository.getOrCreateUser(firebaseUser) } returns approvedAppUser

                    // When
                    listenerSlot.captured.onAuthStateChanged(firebaseAuth)
                    advanceUntilIdle()

                    // Then
                    authRepository.authUiState.value shouldBe AuthUiState.Authenticated(firebaseUser)
                }
            }

            `when`("user is not approved") {
                then("state is NeedsApproval") {
                    // Given
                    val firebaseUser = mockk<FirebaseUser>()
                    val notApprovedAppUser = mockk<AppUser> { every { approved } returns false }
                    val tokenResult = mockk<GetTokenResult>()
                    every { firebaseAuth.currentUser } returns firebaseUser
                    every { firebaseUser.getIdToken(true) } returns Tasks.forResult(tokenResult)
                    coEvery { userRepository.getOrCreateUser(firebaseUser) } returns notApprovedAppUser

                    // When
                    listenerSlot.captured.onAuthStateChanged(firebaseAuth)
                    advanceUntilIdle()

                    // Then
                    authRepository.authUiState.value shouldBe AuthUiState.NeedsApproval
                }
            }
        }

        "signOut calls firebaseAuth signOut" {
            // When
            authRepository.signOut()

            // Then
            verify { firebaseAuth.signOut() }
        }

        "signInWithEmailAndPassword calls firebaseAuth" {
            // Given
            val email = "test@example.com"
            val password = "password"
            val authResult = mockk<AuthResult>()
            every { firebaseAuth.signInWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)

            // When
            authRepository.signInWithEmailAndPassword(email, password)

            // Then
            verify { firebaseAuth.signInWithEmailAndPassword(email, password) }
        }

        "createUserWithEmailAndPassword creates user and app user" {
            // Given
            val email = "test@example.com"
            val password = "password"
            val authResult = mockk<AuthResult>()
            val firebaseUser = mockk<FirebaseUser>()
            every { firebaseAuth.createUserWithEmailAndPassword(email, password) } returns Tasks.forResult(authResult)
            every { authResult.user } returns firebaseUser
            coEvery { userRepository.getOrCreateUser(firebaseUser) } returns mockk()

            // When
            authRepository.createUserWithEmailAndPassword(email, password)

            // Then
            verify { firebaseAuth.createUserWithEmailAndPassword(email, password) }
            coVerify { userRepository.getOrCreateUser(firebaseUser) }
        }

        given("forceReloadAndCheckUserStatus") {
            `when`("user is approved") {
                then("state is Authenticated") {
                    // Given
                    val firebaseUser = mockk<FirebaseUser>()
                    val approvedAppUser = mockk<AppUser> { every { approved } returns true }
                    val tokenResult = mockk<GetTokenResult>()
                    every { firebaseAuth.currentUser } returns firebaseUser
                    every { firebaseUser.getIdToken(true) } returns Tasks.forResult(tokenResult)
                    coEvery { userRepository.getOrCreateUser(firebaseUser) } returns approvedAppUser

                    // When
                    authRepository.forceReloadAndCheckUserStatus()
                    advanceUntilIdle()

                    // Then
                    verify { firebaseUser.getIdToken(true) }
                    coVerify { userRepository.getOrCreateUser(firebaseUser) }
                    authRepository.authUiState.value shouldBe AuthUiState.Authenticated(firebaseUser)
                }
            }

            `when`("user is not approved") {
                then("state is NeedsApproval") {
                    // Given
                    val firebaseUser = mockk<FirebaseUser>()
                    val notApprovedAppUser = mockk<AppUser> { every { approved } returns false }
                    val tokenResult = mockk<GetTokenResult>()
                    every { firebaseAuth.currentUser } returns firebaseUser
                    every { firebaseUser.getIdToken(true) } returns Tasks.forResult(tokenResult)
                    coEvery { userRepository.getOrCreateUser(firebaseUser) } returns notApprovedAppUser

                    // When
                    authRepository.forceReloadAndCheckUserStatus()
                    advanceUntilIdle()

                    // Then
                    authRepository.authUiState.value shouldBe AuthUiState.NeedsApproval
                }
            }
        }
    }
})
