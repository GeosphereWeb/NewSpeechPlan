package de.geosphere.speechplaning.ui.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.geosphere.speechplaning.data.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.repository.authentication.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Definiere diesen lokalen UI-Zustand am besten außerhalb oder oberhalb des ViewModels.
// Er beschreibt nur den Zustand einer *Aktion*, nicht den globalen App-Zustand.
data class AuthActionUiState(
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
    private val _actionUiState = MutableStateFlow(AuthActionUiState())
    val actionUiState: StateFlow<AuthActionUiState> = _actionUiState.asStateFlow()

    fun createUserWithEmailAndPassword(email: String, password: String, name: String) {
        viewModelScope.launch {
            _actionUiState.value = AuthActionUiState(isLoading = true)
            try {
                authRepository.createUserWithEmailAndPassword(email, password, name)
                // Erfolg wird durch den globalen `authUiState` signalisiert,
                // wir müssen hier nichts weiter tun. Der `AuthStateListener` übernimmt.
                _actionUiState.value = AuthActionUiState(isSuccess = true) // Optional für Navigation
            } catch (e: FirebaseAuthUserCollisionException) {
                _actionUiState.value = AuthActionUiState(
                    error = "Ein Konto mit dieser E-Mail existiert bereits." +
                        " $e"
                )
            } catch (e: Exception) {
                _actionUiState.value = AuthActionUiState(
                    error = "Registrierung fehlgeschlagen: " +
                        "${e.localizedMessage}"
                )
            }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _actionUiState.value = AuthActionUiState(isLoading = true)
            try {
                authRepository.signInWithEmailAndPassword(email, password)
                // Erfolg wird ebenfalls durch den `authUiState` signalisiert.
                _actionUiState.value = AuthActionUiState(isSuccess = true) // Optional für Navigation
            } catch (e: Exception) {
                _actionUiState.value = AuthActionUiState(
                    error = "Anmeldung fehlgeschlagen: " +
                        "${e.localizedMessage}"
                )
            }
        }
    }

    // Nützlich, um Fehlermeldungen zurückzusetzen, nachdem sie angezeigt wurden.
    fun resetActionState() {
        _actionUiState.value = AuthActionUiState()
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun checkUserStatusAgain() { // Besserer Name als forceReloadUser
        viewModelScope.launch {
            _actionUiState.value = AuthActionUiState(isLoading = true)

            authRepository.forceReloadAndCheckUserStatus()

            _actionUiState.value = AuthActionUiState(isLoading = false)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _actionUiState.value = AuthActionUiState(isLoading = true)
            val result = authRepository.googleSignIn(activity)
            result.onSuccess {
                // Der AuthStateListener übernimmt von hier an. Wir müssen nur den Ladezustand beenden.
                _actionUiState.value = AuthActionUiState(isLoading = false)
            }.onFailure { exception ->
                _actionUiState.value = AuthActionUiState(error = exception.localizedMessage, isLoading = false)
            }
        }
    }
}
