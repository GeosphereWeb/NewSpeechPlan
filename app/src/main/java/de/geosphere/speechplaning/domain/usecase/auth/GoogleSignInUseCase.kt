package de.geosphere.speechplaning.domain.usecase.auth

import android.app.Activity
import com.google.firebase.auth.GoogleAuthProvider
import de.geosphere.speechplaning.data.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.repository.authentication.UserRepository
import de.geosphere.speechplaning.domain.util.GoogleIdTokenParser // NEUER IMPORT
@Suppress("UseCheckOrError")
class GoogleSignInUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val googleIdTokenParser: GoogleIdTokenParser // NEUE ABHÄNGIGKEIT
) {
    /**
     * Verwaltet den Google Sign-In-Prozess...
     */
    suspend operator fun invoke(activity: Activity): Result<Unit> {
        return runCatching {
            // 1. Google ID Credential abrufen
            val credentialResponse = authRepository.getGoogleIdCredential(activity)

            // 2. ID-Token über den Parser extrahieren (KEIN STATISCHER AUFRUF MEHR)
            val googleIdToken = googleIdTokenParser.parseIdToken(credentialResponse.credential.data)

            // 3. Google ID Token gegen Firebase Auth Credentials tauschen
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

            // 4. Bei Firebase anmelden
            authRepository.signInWithFirebaseCredential(firebaseCredential)

            // 5. Sicherstellen, dass der Benutzer existiert
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser != null) {
                userRepository.getOrCreateUser(firebaseUser)
            } else {
                throw IllegalStateException("User not found after successful Google sign-in.")
            }
            Unit
        }
    }
}
