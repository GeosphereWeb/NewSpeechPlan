package de.geosphere.speechplaning.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.geosphere.speechplaning.data.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.repository.authentication.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Definiere diesen lokalen UI-Zustand am besten außerhalb oder oberhalb des ViewModels.
// Er beschreibt nur den Zustand einer *Aktion*, nicht den globalen App-Zustand.
data class LoginActionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false // Um eine Aktion wie Navigation auszulösen
)

@Suppress("TooGenericExceptionCaught")
open class AuthViewModel(
    private val authRepository: AuthRepository // Injiziert per Koin
) : ViewModel() {

    // Globaler Authentifizierungs-Zustand aus dem Repository
    val authUiState: StateFlow<AuthUiState> = authRepository.authUiState

    // Lokaler Zustand für Aktionen wie Registrierung oder Anmeldung
    private val _loginActionUiState = MutableStateFlow(LoginActionUiState())
    val loginActionUiState: StateFlow<LoginActionUiState> = _loginActionUiState

    fun createUserWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _loginActionUiState.value = LoginActionUiState(isLoading = true)
            try {
                authRepository.createUserWithEmailAndPassword(email, password)
                // Erfolg wird durch den globalen `authUiState` signalisiert,
                // wir müssen hier nichts weiter tun. Der `AuthStateListener` übernimmt.
                _loginActionUiState.value = LoginActionUiState(isSuccess = true) // Optional für Navigation
            } catch (e: FirebaseAuthUserCollisionException) {
                _loginActionUiState.value = LoginActionUiState(
                    error = "Ein Konto mit dieser E-Mail existiert bereits." +
                        " $e"
                )
            } catch (e: Exception) {
                _loginActionUiState.value = LoginActionUiState(
                    error = "Registrierung fehlgeschlagen: " +
                        "${e.localizedMessage}"
                )
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _loginActionUiState.value = LoginActionUiState(isLoading = true)
            try {
                authRepository.signInWithEmailAndPassword(email, password)
                // Erfolg wird ebenfalls durch den `authUiState` signalisiert.
                _loginActionUiState.value = LoginActionUiState(isSuccess = true) // Optional für Navigation
            } catch (e: Exception) {
                _loginActionUiState.value = LoginActionUiState(
                    error = "Anmeldung fehlgeschlagen: " +
                        "${e.localizedMessage}"
                )
            }
        }
    }

    // Nützlich, um Fehlermeldungen zurückzusetzen, nachdem sie angezeigt wurden.
    fun resetActionState() {
        _loginActionUiState.value = LoginActionUiState()
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun checkUserStatusAgain() { // Besserer Name als forceReloadUser
        viewModelScope.launch {
            // Zeige dem User, dass etwas passiert (optional, aber gut für UX)
            // _loginActionUiState.value = LoginActionUiState(isLoading = true)

            authRepository.forceReloadAndCheckUserStatus()

            // Ladezustand zurücksetzen
            // _loginActionUiState.value = LoginActionUiState(isLoading = false)
        }
    }
}
