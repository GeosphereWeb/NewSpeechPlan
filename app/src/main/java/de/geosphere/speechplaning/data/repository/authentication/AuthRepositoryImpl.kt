package de.geosphere.speechplaning.data.repository.authentication

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Suppress("TooGenericExceptionCaught")
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val externalScope: CoroutineScope,
    private val context: Context // Wird für den CredentialManager benötigt
) : AuthRepository {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    override val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        externalScope.launch {
            try {
                val user = firebaseAuth.currentUser
                if (user == null) {
                    // Benutzer ist nicht angemeldet.
                    _authUiState.value = AuthUiState.Unauthenticated
                    return@launch
                }

                // ========================================================================
                // ===== HIER IST DER ENTSCHEIDENDE TEIL =====
                //
                // Diese Zeile weist Firebase an, das ID-Token des Benutzers
                // vom Server neu abzurufen. Das Argument `true` erzwingt die
                // Aktualisierung aus dem Netzwerk und ignoriert den lokalen Cache.
                // Dieser Aufruf aktualisiert den gesamten internen Zustand des
                // `currentUser`-Objekts, einschließlich der Informationen, die
                // du serverseitig geändert hast (indirekt über das Token).
                // ========================================================================
                user.getIdToken(true).await()

                // Nachdem das Token aktualisiert wurde, hat die App nun die
                // neuesten Informationen vom Server.
                val appUser = userRepository.getOrCreateUser(user)

                // Jetzt sollte die Prüfung des 'approved'-Status korrekt funktionieren.
                if (appUser.approved) {
                    _authUiState.value = AuthUiState.Authenticated(user)
                } else {
                    _authUiState.value = AuthUiState.NeedsApproval
                }
            } catch (e: Exception) {
                // Fehlerbehandlung: Wenn etwas schiefgeht (z.B. Netzwerkfehler
                // beim Neuladen des Tokens), den Benutzer sicherheitshalber ausloggen.
                Log.e("AuthRepository", "Fehler bei der Überprüfung des Auth-Status", e)
                firebaseAuth.signOut()
                _authUiState.value = AuthUiState.Unauthenticated
            }
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override suspend fun createUserWithEmailAndPassword(email: String, password: String, name: String) {
        // Schritt 1: Firebase Auth Benutzer erstellen
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user

        // Wenn die Erstellung fehlschlägt, wirft .await() eine Exception, die wir im ViewModel fangen.
        // Wenn sie erfolgreich ist, aber der User null ist (sollte nie passieren), werfen wir einen Fehler.
        val nonNullFirebaseUser = checkNotNull(firebaseUser) {
            "Firebase user creation succeeded but user is null."
        }

        // Schritt 2: Den Anzeigenamen aktualisieren
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        nonNullFirebaseUser.updateProfile(profileUpdates).await()

        // Schritt 3: Den zugehörigen AppUser-Eintrag in Firestore erstellen.
        // 'nonNullFirebaseUser' ist garantiert nicht null.
        userRepository.getOrCreateUser(nonNullFirebaseUser)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        // Die Anmeldung ändert nur den auth state. Der AuthStateListener
        // kümmert sich dann automatisch um den Rest (Nutzerdaten laden, `approved` prüfen etc.).
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    override suspend fun signOut() {
        // Meldet den Benutzer bei Firebase ab.
        firebaseAuth.signOut()

        // Meldet den Benutzer über den CredentialManager ab, um den One-Tap-Zustand zurückzusetzen.
        // Dies ersetzt den veralteten GoogleSignInClient.signOut().
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Fehler beim Abmelden über CredentialManager", e)
        }
    }

    // --- NEUE Implementierung für Google Sign-In ---
    override suspend fun googleSignIn(activity: Activity): Result<Unit> {
        // 1. Hole deine Web-Client-ID aus den Strings. Diese ist in der google-services.json zu finden.
        val serverClientId = "252698290911-0tcqq62onf1mr5pkcgomktftfb7muicb.apps.googleusercontent.com"

        // 2. Erstelle eine Anfrage für ein Google ID-Token.
        val getGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false) // Zeigt alle Google-Konten auf dem Gerät
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true) // Versucht, automatisch ein Konto auszuwählen (One-Tap)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(getGoogleIdOption)
            .build()

        // 3. Führe die Anfrage mit dem CredentialManager aus.
        return try {
            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(activity, request) // Benötigt eine Activity
            handleGoogleSignInResult(result)
        } catch (e: GetCredentialException) {
            // Behandelt Fehler wie "Nutzer hat den Dialog geschlossen" etc.
            Result.failure(e)
        }
    }

    private suspend fun handleGoogleSignInResult(result: GetCredentialResponse): Result<Unit> {
        val credential = result.credential
        val googleIdToken =
            com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                .idToken

        // 4. Tausche das ID-Token bei Firebase Auth gegen Firebase-Credentials ein.
        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        return try {
            firebaseAuth.signInWithCredential(firebaseCredential).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forceReloadAndCheckUserStatus() {
        try {
            val user = firebaseAuth.currentUser ?: return // Wenn kein User da, nichts tun

            // 1. Lade die neuesten Daten vom Server
            user.getIdToken(true).await()

            // 2. Führe die gleiche Logik wie im AuthStateListener erneut aus!
            val appUser = userRepository.getOrCreateUser(user)
            if (appUser.approved) {
                _authUiState.value = AuthUiState.Authenticated(user)
            } else {
                // Falls der Admin die Freigabe widerrufen hat, bleibe hier
                _authUiState.value = AuthUiState.NeedsApproval
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Fehler bei der manuellen Statusprüfung", e)
            // Optional: Fehler an die UI melden, z.B. über einen separaten State
        }
    }
}
