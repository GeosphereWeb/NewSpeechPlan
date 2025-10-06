package de.geosphere.speechplaning.data.repository.authentication

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
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
    private val externalScope: CoroutineScope
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

    override suspend fun createUserWithEmailAndPassword(email: String, password: String) {
        // Schritt 1: Firebase Auth Benutzer erstellen
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user

        // Wenn die Erstellung fehlschlägt, wirft .await() eine Exception, die wir im ViewModel fangen.
        // Wenn sie erfolgreich ist, aber der User null ist (sollte nie passieren), werfen wir einen Fehler.
        val nonNullFirebaseUser = checkNotNull(firebaseUser) {
            "Firebase user creation succeeded but user is null."
        }

        // Schritt 2: Den zugehörigen AppUser-Eintrag in Firestore erstellen.
        // 'nonNullFirebaseUser' ist garantiert nicht null.
        userRepository.getOrCreateUser(nonNullFirebaseUser)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        // Die Anmeldung ändert nur den auth state. Der AuthStateListener
        // kümmert sich dann automatisch um den Rest (Nutzerdaten laden, `approved` prüfen etc.).
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
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
