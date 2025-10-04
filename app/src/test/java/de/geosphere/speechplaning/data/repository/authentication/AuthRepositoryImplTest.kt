package de.geosphere.speechplaning.data.repository.authentication

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GetTokenResult
import de.geosphere.speechplaning.data.model.AppUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AuthRepositoryImplTest {

    @RelaxedMockK
    private lateinit var firebaseAuth: FirebaseAuth

    @RelaxedMockK
    private lateinit var userRepository: UserRepository

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository

    // Slot to capture the listener
    private val listenerSlot = slot<FirebaseAuth.AuthStateListener>()

    @BeforeEach
    fun setUp() {
        // Capture the listener upon initialization of the repository
        every { firebaseAuth.addAuthStateListener(capture(listenerSlot)) } returns Unit
        authRepository = AuthRepositoryImpl(firebaseAuth, userRepository, CoroutineScope(testDispatcher))
    }

    @Test
    fun `AuthStateListener - when user is null, state is Unauthenticated`() = runTest(testDispatcher) {
        // Given an unauthenticated user
        every { firebaseAuth.currentUser } returns null

        // When the listener is triggered
        listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        advanceUntilIdle()

        // Then the state should be Unauthenticated
        assertEquals(AuthUiState.Unauthenticated, authRepository.authUiState.value)
    }

    @Test
    fun `AuthStateListener - when user is approved, state is Authenticated`() = runTest(testDispatcher) {
        // Given a logged in and approved user
        val firebaseUser = mockk<FirebaseUser>()
        val approvedAppUser = mockk<AppUser> { every { approved } returns true }
        val tokenResult = mockk<GetTokenResult>()
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.getIdToken(true) } returns Tasks.forResult(tokenResult)
        coEvery { userRepository.getOrCreateUser(firebaseUser) } returns approvedAppUser

        // When the listener is triggered
        listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        advanceUntilIdle()

        // Then the state should be Authenticated
        assertEquals(AuthUiState.Authenticated(firebaseUser), authRepository.authUiState.value)
    }

    @Test
    fun `AuthStateListener - when user is not approved, state is NeedsApproval`() = runTest(testDispatcher) {
        // Given a logged in but not approved user
        val firebaseUser = mockk<FirebaseUser>()
        val notApprovedAppUser = mockk<AppUser> { every { approved } returns false }
        val tokenResult = mockk<GetTokenResult>()
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.getIdToken(true) } returns Tasks.forResult(tokenResult)
        coEvery { userRepository.getOrCreateUser(firebaseUser) } returns notApprovedAppUser

        // When the listener is triggered
        listenerSlot.captured.onAuthStateChanged(firebaseAuth)
        advanceUntilIdle()

        // Then the state should be NeedsApproval
        assertEquals(AuthUiState.NeedsApproval, authRepository.authUiState.value)
    }

    @Test
    fun `signOut calls firebaseAuth signOut`() {
        // When
        authRepository.signOut()

        // Then
        verify { firebaseAuth.signOut() }
    }

    @Test
    fun `signInWithEmailAndPassword calls firebaseAuth`() = runTest {
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

    @Test
    fun `createUserWithEmailAndPassword creates user and app user`() = runTest {
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

    @Test
    fun `forceReloadAndCheckUserStatus - when user approved, state is Authenticated`() = runTest(testDispatcher) {
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
        assertEquals(AuthUiState.Authenticated(firebaseUser), authRepository.authUiState.value)
    }

    @Test
    fun `forceReloadAndCheckUserStatus - when user not approved, state is NeedsApproval`() = runTest(testDispatcher) {
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
        assertEquals(AuthUiState.NeedsApproval, authRepository.authUiState.value)
    }
}
