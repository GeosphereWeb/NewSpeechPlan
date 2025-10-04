package de.geosphere.speechplaning.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Repräsentiert den UI-Zustand (z.B. Ladezustand, Erfolg, Fehler)
data class AuthUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

open class AuthViewModel(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    open val uiState: StateFlow<AuthUiState> = _uiState

    // Registriert einen neuen Benutzer
    open fun createUserWithEmailAndPassword(email: String, passwort: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                auth.createUserWithEmailAndPassword(email, passwort).await()
                _uiState.value = AuthUiState(success = true)
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.value = AuthUiState(error = "Ein Konto mit dieser E-Mail existiert bereits.")
            } catch (e: Exception) {
                _uiState.value = AuthUiState(error = "Registrierung fehlgeschlagen: ${e.message}")
            }
        }
    }

    // Meldet einen bestehenden Benutzer an
    open fun signInWithEmailAndPassword(email: String, passwort: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email, passwort).await()
                _uiState.value = AuthUiState(success = true)
            } catch (e: Exception) {
                // ALT:
                // _uiState.value = AuthUiState(error = "Anmeldung fehlgeschlagen: ${e.message}")

                // NEU: Zeige die spezifische Fehlermeldung an
                _uiState.value = AuthUiState(error = "Fehler: ${e.localizedMessage}")
            }
        }
    }
}
