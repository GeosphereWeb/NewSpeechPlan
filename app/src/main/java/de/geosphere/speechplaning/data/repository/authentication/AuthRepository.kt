package de.geosphere.speechplaning.data.repository.authentication

import android.app.Activity
import androidx.credentials.GetCredentialResponse
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authUiState: StateFlow<AuthUiState>
    fun getCurrentUser(): FirebaseUser?
    suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser
    suspend fun updateFirebaseUserProfile(user: FirebaseUser, name: String)
    suspend fun signInWithEmailAndPassword(email: String, password: String)
    suspend fun signOutFirebase()
    suspend fun clearCredentialManager()
    suspend fun getGoogleIdCredential(activity: Activity): GetCredentialResponse
    suspend fun signInWithFirebaseCredential(credential: AuthCredential)
    suspend fun reloadFirebaseUserToken(user: FirebaseUser)
    suspend fun forceReloadAndCheckUserStatus()
}
