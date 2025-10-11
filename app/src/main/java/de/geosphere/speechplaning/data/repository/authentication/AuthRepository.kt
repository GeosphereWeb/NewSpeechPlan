package de.geosphere.speechplaning.data.repository.authentication

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authUiState: StateFlow<AuthUiState>

    suspend fun createUserWithEmailAndPassword(email: String, password: String, name: String)

    suspend fun signInWithEmailAndPassword(email: String, password: String)

    suspend fun signOut()

    suspend fun googleSignIn(activity: Activity): Result<Unit>

    suspend fun forceReloadAndCheckUserStatus()
}

// sealed class AuthUiState {
//     object Loading : AuthUiState()
//     data class Authenticated(val user: FirebaseUser) : AuthUiState()
//     object Unauthenticated : AuthUiState()
//     object NeedsApproval : AuthUiState()
// }
