package de.geosphere.speechplaning.data.repository

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.AuthUiState
import de.geosphere.speechplaning.data.model.AppUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("TooGenericExceptionCaught")
@ExperimentalCoroutinesApi
internal class AuthRepositoryImplTest {

    // Mocks für die Abhängigkeiten
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUserRepository: UserRepository

    // Slot zum Einfangen des AuthStateListeners
    private val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()

    @BeforeEach
    fun setUp() {
        // Mock für android.util.Log
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0

        // Erstellen der Mocks
        mockAuth = mockk()
        mockUserRepository = mockk()

        // Standardverhalten für den AuthStateListener-Slot
        every { mockAuth.addAuthStateListener(capture(authStateListenerSlot)) } just runs
        every { mockAuth.signOut() } just runs // Wir erwarten, dass signOut() aufgerufen werden kann
    }

    @AfterEach
    fun tearDown() {
        // Aufheben des Mocks nach jedem Test
        unmockkStatic(Log::class)
    }

    @Test
    fun `when firebase user is null, state should be Unauthenticated`() = runTest {
        // KORREKTUR: Repository hier initialisieren, um den Scope von runTest zu verwenden
        val authRepository = AuthRepositoryImpl(mockAuth, mockUserRepository, this)

        // 1. Arrange: Simuliere, dass kein Nutzer angemeldet ist
        val mockFirebaseAuth = mockk<FirebaseAuth>()
        every { mockFirebaseAuth.currentUser } returns null

        // 2. Act: Löse den Listener mit dem Mock aus
        authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
        advanceUntilIdle()

        // 3. Assert: Überprüfe, ob der Zustand korrekt ist
        assertEquals(AuthUiState.Unauthenticated, authRepository.authUiState.value)
    }

    @Test
    fun `when user reload fails, state should be Unauthenticated and user signed out`() = runTest {
        val authRepository = AuthRepositoryImpl(mockAuth, mockUserRepository, this)

        // 1. Arrange: Simuliere einen angemeldeten Nutzer, aber einen Fehler beim reload()
        val mockUser = mockk<FirebaseUser>()
        val mockFirebaseAuth = mockk<FirebaseAuth>()
        every { mockFirebaseAuth.currentUser } returns mockUser

        // KORREKTUR: Verwende eine echte, fehlgeschlagene Task
        val reloadException = Exception("Reload failed")
        every { mockUser.reload() } returns Tasks.forException(reloadException)

        // 2. Act: Löse den Listener aus
        authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
        advanceUntilIdle()

        // 3. Assert: Überprüfe den Zustand und die Interaktionen
        assertEquals(AuthUiState.Unauthenticated, authRepository.authUiState.value)
        verify(exactly = 1) { mockAuth.signOut() } // Stelle sicher, dass der Nutzer abgemeldet wurde
    }

    @Test
    fun `when user is approved, state should be Authenticated`() = runTest {
        val authRepository = AuthRepositoryImpl(mockAuth, mockUserRepository, this)

        // 1. Arrange: Simuliere einen angemeldeten und freigegebenen Nutzer
        val mockUser = mockk<FirebaseUser>()
        val mockFirebaseAuth = mockk<FirebaseAuth>()
        val approvedAppUser = AppUser(approved = true)

        every { mockFirebaseAuth.currentUser } returns mockUser
        // KORREKTUR: Verwende eine echte, erfolgreiche Task
        every { mockUser.reload() } returns Tasks.forResult(null)
        coEvery { mockUserRepository.getOrCreateUser(mockUser) } returns approvedAppUser

        // 2. Act: Löse den Listener aus
        authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
        advanceUntilIdle()

        // 3. Assert: Überprüfe den Zustand
        val state = authRepository.authUiState.value
        assertInstanceOf(AuthUiState.Authenticated::class.java, state)
        assertEquals(mockUser, (state as AuthUiState.Authenticated).user)
    }

    @Test
    fun `when user is not approved, state should be NeedsApproval`() = runTest {
        val authRepository = AuthRepositoryImpl(mockAuth, mockUserRepository, this)

        // 1. Arrange: Simuliere einen angemeldeten, aber nicht freigegebenen Nutzer
        val mockUser = mockk<FirebaseUser>()
        val mockFirebaseAuth = mockk<FirebaseAuth>()
        val unapprovedAppUser = AppUser(approved = false)

        every { mockFirebaseAuth.currentUser } returns mockUser
        every { mockUser.reload() } returns Tasks.forResult(null) // Erfolgreiches Reload
        coEvery { mockUserRepository.getOrCreateUser(mockUser) } returns unapprovedAppUser

        // 2. Act: Löse den Listener aus
        authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
        advanceUntilIdle()

        // 3. Assert: Überprüfe den Zustand
        assertEquals(AuthUiState.NeedsApproval, authRepository.authUiState.value)
    }

    @Test
    fun `when getOrCreateUser fails, state should be Unauthenticated`() = runTest {
        val authRepository = AuthRepositoryImpl(mockAuth, mockUserRepository, this)

        // 1. Arrange: Simuliere einen Fehler beim Abrufen der Nutzerdaten
        val mockUser = mockk<FirebaseUser>()
        val mockFirebaseAuth = mockk<FirebaseAuth>()

        every { mockFirebaseAuth.currentUser } returns mockUser
        every { mockUser.reload() } returns Tasks.forResult(null) // Erfolgreiches Reload
        coEvery { mockUserRepository.getOrCreateUser(mockUser) } throws Exception("Firestore error")

        // 2. Act: Löse den Listener aus
        authStateListenerSlot.captured.onAuthStateChanged(mockFirebaseAuth)
        advanceUntilIdle()

        // 3. Assert: Überprüfe den Zustand und die Interaktionen
        assertEquals(AuthUiState.Unauthenticated, authRepository.authUiState.value)
        verify(exactly = 1) { mockAuth.signOut() } // Stelle sicher, dass der Nutzer abgemeldet wurde
    }
}
