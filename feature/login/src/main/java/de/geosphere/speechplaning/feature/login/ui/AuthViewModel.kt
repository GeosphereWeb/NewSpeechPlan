package de.geosphere.speechplaning.feature.login.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import de.geosphere.speechplaning.data.authentication.AuthRepository
import de.geosphere.speechplaning.data.authentication.AuthUiState
import de.geosphere.speechplaning.data.usecases.CreateUserWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.GoogleSignInUseCase
import de.geosphere.speechplaning.data.usecases.SignInWithEmailAndPasswordUseCase
import de.geosphere.speechplaning.data.usecases.SignOutUseCase
import kotlinx.coroutines.flow.Flow
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
    private val createUserWithEmailAndPasswordUseCase: CreateUserWithEmailAndPasswordUseCase,
    private val signInWithEmailAndPasswordUseCase: SignInWithEmailAndPasswordUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authRepository: AuthRepository // Behalten für den authUiState und forceReload
) : ViewModel() {
    // Globaler Authentifizierungs-Zustand aus dem Repository
    fun getAuthUiState(): Flow<AuthUiState> = authRepository.authUiState

    // Lokaler Zustand für Aktionen wie Registrierung oder Anmeldung
    private val _actionUiState = MutableStateFlow(AuthActionUiState())
    val actionUiState: StateFlow<AuthActionUiState> = _actionUiState.asStateFlow()

    fun createUserWithEmailAndPassword(email: String, password: String, name: String) {
        viewModelScope.launch {
            _actionUiState.value = AuthActionUiState(isLoading = true)

            createUserWithEmailAndPasswordUseCase(email, password, name)
                .onSuccess {
                    // Der globale authUiState wird die erfolgreiche Anmeldung signalisieren.
                    // Wir setzen hier isSuccess, um z.B. eine Navigation auszulösen.
                    _actionUiState.value = AuthActionUiState(isSuccess = true)
                }
                .onFailure { exception ->
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> "Ein Konto mit dieser E-Mail existiert bereits."
                        is IllegalArgumentException -> exception.message // Die Nachricht aus dem UseCase
                        else -> "Registrierung fehlgeschlagen: ${exception.localizedMessage}"
                    }
                    _actionUiState.value = AuthActionUiState(error = errorMessage)
                }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _actionUiState.value = AuthActionUiState(isLoading = true)

            signInWithEmailAndPasswordUseCase(email, password)
                .onSuccess {
                    // Erfolg wird ebenfalls durch den `authUiState` signalisiert.
                    _actionUiState.value = AuthActionUiState(isSuccess = true) // Optional für Navigation
                }
                .onFailure { exception ->
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> "E-Mail oder Passwort ist falsch."
                        is IllegalArgumentException -> exception.message // Die Nachricht aus dem UseCase
                        else -> "Anmeldung fehlgeschlagen: ${exception.localizedMessage}"
                    }
                    _actionUiState.value = AuthActionUiState(error = errorMessage)
                }
        }
    }

    // Nützlich, um Fehlermeldungen zurückzusetzen, nachdem sie angezeigt wurden.
    fun resetActionState() {
        _actionUiState.value = AuthActionUiState()
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess {
                    // Der AuthStateListener im Repository kümmert sich um die Aktualisierung des globalen Zustands.
                    // Wir müssen hier nichts weiter tun.
                }
                .onFailure { exception ->
                    // Optional: Fehler im UI anzeigen, wenn der Logout fehlschlägt.
                    _actionUiState.value = AuthActionUiState(
                        error = "Abmeldung fehlgeschlagen: " +
                            "${exception.localizedMessage}"
                    )
                }
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
            val result = googleSignInUseCase(activity)
            result.onSuccess {
                // Der AuthStateListener übernimmt von hier an. Wir müssen nur den Ladezustand beenden.
                _actionUiState.value = AuthActionUiState(isLoading = false)
            }.onFailure { exception ->
                _actionUiState.value = AuthActionUiState(error = exception.localizedMessage, isLoading = false)
            }
        }
    }
}
