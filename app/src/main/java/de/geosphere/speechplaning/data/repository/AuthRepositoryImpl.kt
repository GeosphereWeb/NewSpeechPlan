package de.geosphere.speechplaning.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.AuthUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Suppress("TooGenericExceptionCaught")
class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val externalScope: CoroutineScope
) : AuthRepository {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    override val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    override fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authUiState.value = AuthUiState.Unauthenticated
        } else {
            externalScope.launch {
                onSignInSuccess(currentUser)
            }
        }
    }

    override suspend fun onSignInSuccess(user: FirebaseUser) {
        _authUiState.value = AuthUiState.Loading
        try {
            // user.reload().await() // ENTFERNT: Diese Zeile verursachte die Race Condition
            val appUser = userRepository.getOrCreateUser(user)

            if (appUser.approved) {
                _authUiState.value = AuthUiState.Authenticated(user)
            } else {
                _authUiState.value = AuthUiState.NeedsApproval
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during sign-in processing", e)
            signOut()
            _authUiState.value = AuthUiState.Error("Fehler bei der Anmeldung: ${e.message}")
        }
    }

    override fun signOut() {
        auth.signOut()
        _authUiState.value = AuthUiState.Unauthenticated
    }
}
