package de.geosphere.speechplaning.data.repository.authentication

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import de.geosphere.speechplaning.domain.usecase.auth.DetermineAppUserStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("TooGenericExceptionCaught")
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val externalScope: CoroutineScope,
    private val context: Context,
    private val determineAppUserStatusUseCase: DetermineAppUserStatusUseCase // Bleibt als Abhängigkeit
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

                // Verwende den UseCase und übergebe die Low-Level-Reload-Aktion
                _authUiState.value = determineAppUserStatusUseCase(
                    firebaseUser = user,
                    // authStateListener soll in der Regel keinen erzwungenen Reload
                    // machen, da er bei Änderungen triggert
                    forceReload = false,
                    reloadTokenAction = { firebaseUserToReload ->
                        // Hier rufen wir die eigene Low-Level-Methode des Repositories auf
                        reloadFirebaseUserToken(firebaseUserToReload)
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthRepository", "Fehler bei der Überprüfung des Auth-Status", e)
                firebaseAuth.signOut() // Nur Firebase-Abmeldung
                _authUiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // --- NEUE/ANGEPASSTE Low-Level-Methoden für Firebase Auth und CredentialManager ---

    // Erstellt nur den Firebase-Benutzer, keine Profil-Updates oder userRepository-Aufrufe hier
    override suspend fun createUserWithEmailAndPassword(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return checkNotNull(result.user) { "Firebase user creation succeeded but user is null." }
    }

    // Low-Level-Methode zum Aktualisieren des Firebase-Benutzerprofils
    override suspend fun updateFirebaseUserProfile(user: FirebaseUser, name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdates).await()
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    // Nur Firebase-Abmeldung
    override suspend fun signOutFirebase() {
        firebaseAuth.signOut()
    }

    // Nur CredentialManager-Abmeldung
    override suspend fun clearCredentialManager() {
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Fehler beim Abmelden über CredentialManager", e)
            // Fehler hier abfangen, da dies nicht unbedingt ein Kritischer Fehler für den Abmeldevorgang ist
        }
    }

    // Low-Level-Methode zum Abrufen eines Google ID Credentials
    override suspend fun getGoogleIdCredential(activity: Activity): GetCredentialResponse {
        // Diese ID bleibt hier, da sie eine Konfigurationsabhängigkeit des Repositories ist
        val serverClientId =
            "252698290911-0tcqq62onf1mr5pkcgomktftfb7muicb.apps.googleusercontent.com"

        val getGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(getGoogleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)
        return credentialManager.getCredential(activity, request)
    }

    // Low-Level-Methode zum Anmelden bei Firebase mit beliebigen AuthCredentials
    override suspend fun signInWithFirebaseCredential(credential: AuthCredential) {
        firebaseAuth.signInWithCredential(credential).await()
    }

    override suspend fun reloadFirebaseUserToken(user: FirebaseUser) {
        user.getIdToken(true).await()
    }

    override suspend fun forceReloadAndCheckUserStatus() {
        try {
            val user = firebaseAuth.currentUser ?: return

            // Verwende den UseCase mit forceReload = true und gib die Reload-Aktion an
            _authUiState.value = determineAppUserStatusUseCase(
                firebaseUser = user,
                forceReload = true,
                reloadTokenAction = { firebaseUserToReload ->
                    // Hier rufen wir die eigene Low-Level-Methode des Repositories auf
                    reloadFirebaseUserToken(firebaseUserToReload)
                }
            )
        } catch (e: Exception) {
            Log.e("AuthRepository", "Fehler bei der manuellen Statusprüfung", e)
            _authUiState.value = AuthUiState.Unauthenticated
        }
    }
}
