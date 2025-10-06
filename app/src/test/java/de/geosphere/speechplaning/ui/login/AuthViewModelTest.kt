package de.geosphere.speechplaning.ui.login

import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.geosphere.speechplaning.data.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.repository.authentication.AuthUiState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class AuthViewModelTest {

    @RelaxedMockK
    private lateinit var authRepository: AuthRepository

    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Mock the authUiState to return a controllable StateFlow
        coEvery { authRepository.authUiState } returns MutableStateFlow(AuthUiState.Unauthenticated)
        viewModel = AuthViewModel(authRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createUserWithEmailAndPassword success updates state to success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authRepository.createUserWithEmailAndPassword(email, password) } returns Unit

        // When
        viewModel.createUserWithEmailAndPassword(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginActionUiState.value
        assertTrue(state.isSuccess)
        coVerify { authRepository.createUserWithEmailAndPassword(email, password) }
    }

    @Test
    fun `createUserWithEmailAndPassword failure with collision exception updates state with error`() = runTest {
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
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("existiert bereits"))
    }

    @Test
    fun `createUserWithEmailAndPassword - when generic exception, posts error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val errorMessage = "Ein unerwarteter Fehler ist aufgetreten"
        coEvery { authRepository.createUserWithEmailAndPassword(email, password) } throws Exception(errorMessage)

        // When
        viewModel.createUserWithEmailAndPassword(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginActionUiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains(errorMessage))
    }
    
    @Test
    fun `signInWithEmailAndPassword success updates state to success`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        coEvery { authRepository.signInWithEmailAndPassword(email, password) } returns Unit

        // When
        viewModel.signInWithEmailAndPassword(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginActionUiState.value
        assertTrue(state.isSuccess)
        coVerify { authRepository.signInWithEmailAndPassword(email, password) }
    }

    @Test
    fun `signInWithEmailAndPassword failure updates state with error`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password"
        val errorMessage = "Anmeldung fehlgeschlagen"
        coEvery { authRepository.signInWithEmailAndPassword(email, password) } throws Exception(errorMessage)
        
        // When
        viewModel.signInWithEmailAndPassword(email, password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginActionUiState.value
        assertNotNull(state.error)
        assertTrue(state.error!!.contains(errorMessage))
    }

    @Test
    fun `signOut calls repository signOut`() {
        // When
        viewModel.signOut()

        // Then
        verify { authRepository.signOut() }
    }
    
    @Test
    fun `checkUserStatusAgain calls repository forceReloadAndCheckUserStatus`() = runTest {
        // When
        viewModel.checkUserStatusAgain()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        coVerify { authRepository.forceReloadAndCheckUserStatus() }
    }
    
    @Test
    fun `resetActionState resets the ui state`() = runTest {
        // Given
        // Create an error state first by simulating a failed login
        coEvery { authRepository.signInWithEmailAndPassword(any(), any()) } throws Exception("Failed")
        viewModel.signInWithEmailAndPassword("a", "b")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.resetActionState()
        
        // Then
        val expectedState = LoginActionUiState()
        assertEquals(expectedState, viewModel.loginActionUiState.value)
    }
}
