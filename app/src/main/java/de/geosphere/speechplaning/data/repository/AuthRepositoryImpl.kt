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
    private val externalScope: CoroutineScope
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
                // Die Logik zur Prüfung des `approved`-Status ist hier schon perfekt.
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

    override suspend fun createUserWithEmailAndPassword(email: String, password: String) {
        // Schritt 1: Firebase Auth Benutzer erstellen
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user

        // Wenn die Erstellung fehlschlägt, wirft .await() eine Exception, die wir im ViewModel fangen.
        // Wenn sie erfolgreich ist, aber der User null ist (sollte nie passieren), werfen wir einen Fehler.
        if (firebaseUser == null) {
            throw IllegalStateException("Firebase user creation succeeded but user is null.")
        }

        // Schritt 2: Den zugehörigen AppUser-Eintrag in Firestore erstellen.
        // Deine getOrCreateUser-Methode erledigt das für uns!
        userRepository.getOrCreateUser(firebaseUser)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String) {
        // Die Anmeldung ändert nur den auth state. Der AuthStateListener
        // kümmert sich dann automatisch um den Rest (Nutzerdaten laden, `approved` prüfen etc.).
        auth.signInWithEmailAndPassword(email, password).await()
    }

    override fun signOut() {
        auth.signOut()
    }
}
