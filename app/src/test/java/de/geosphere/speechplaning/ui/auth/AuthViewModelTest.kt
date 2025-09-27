package de.geosphere.speechplaning.ui.auth

import de.geosphere.speechplaning.data.AuthUiState
import de.geosphere.speechplaning.data.repository.AuthRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit-Test für das [AuthViewModel].
 * Da das ViewModel nur den Zustand vom Repository durchreicht, wird hier primär
 * die korrekte Weitergabe des Zustands überprüft.
 */
internal class AuthViewModelTest {

    private lateinit var cut: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeEach
    fun setUp() {
        // 1. Mocken der Abhängigkeit
        cut = mockk()
    }

    @Test
    fun `uiState should reflect the state from AuthRepository`() {
        // 2. Vorbereiten der Testdaten und des Mock-Verhaltens
        val testStateFlow = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
        every { cut.authUiState } returns testStateFlow

        // 3. Erstellen der zu testenden ViewModel-Instanz
        viewModel = AuthViewModel(cut)

        // 4. Überprüfen des Ergebnisses
        // Wir stellen sicher, dass der uiState des ViewModels genau der Flow ist,
        // den das Repository bereitstellt.
        assertEquals(testStateFlow, viewModel.uiState)

        // Zusätzliche Überprüfung des initialen Werts
        assertEquals(AuthUiState.Loading, viewModel.uiState.value)
    }
}
