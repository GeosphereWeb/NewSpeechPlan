package de.geosphere.speechplaning.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import de.geosphere.speechplaning.data.AuthUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("TooGenericExceptionCaught")
class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val externalScope: CoroutineScope // Standardwert entfernt
) : AuthRepository {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    override val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        externalScope.launch {
            try {
                val user = firebaseAuth.currentUser
                if (user == null) {
                    _authUiState.value = AuthUiState.Unauthenticated
                    return@launch
                }

                user.reload().await()
                val appUser = userRepository.getOrCreateUser(user)

                if (appUser.approved) {
                    _authUiState.value = AuthUiState.Authenticated(user)
                } else {
                    _authUiState.value = AuthUiState.NeedsApproval
                }

            } catch (e: Exception) {
                Log.e("AuthRepository", "Error during auth state check", e)
                auth.signOut()
                _authUiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }
}
