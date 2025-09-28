package de.geosphere.speechplaning.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * Das ViewModel, das der UI den Authentifizierungsstatus bereitstellt.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val uiState = authRepository.authUiState

    /**
     * Prüft den aktuellen Anmeldestatus. Wird von der UI einmalig aufgerufen.
     */
    fun checkCurrentUser() {
        authRepository.checkCurrentUser()
    }

    fun onSignInSuccess(user: FirebaseUser) {
        viewModelScope.launch {
            authRepository.onSignInSuccess(user)
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
