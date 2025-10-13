package de.geosphere.speechplaning.domain.usecase.auth

import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.repository.authentication.AuthUiState
import de.geosphere.speechplaning.data.repository.authentication.UserRepository

/**
 * Bestimmt den anwendungsspezifischen Authentifizierungs-UI-Zustand basierend auf einem FirebaseUser.
 * Dies beinhaltet das (optional erzwungene) Neuladen des ID-Tokens und die Überprüfung des internen
 * Genehmigungsstatus des Benutzers in der App-Datenbank.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DetermineAppUserStatusUseCase(
    private val userRepository: UserRepository // Nur noch UserRepository als direkte Abhängigkeit
) {
    suspend operator fun invoke(
        firebaseUser: FirebaseUser,
        forceReload: Boolean = false,
        // NEU: Ein Lambda, das die Aktion zum Neuladen des Tokens kapselt
        reloadTokenAction: (suspend (FirebaseUser) -> Unit)? = null
    ): AuthUiState {
        return try {
            // Geschäftslogik: ID-Token neu laden, wenn explizit angefordert UND eine Aktion dafür bereitgestellt wurde.
            if (forceReload) {
                reloadTokenAction?.invoke(firebaseUser)
            }

            // Geschäftslogik: Den zugehörigen de.geosphere.speechplaning.domain.usecase.auth.AppUser-Eintrag
            // in Firestore abrufen/erstellen.
            val appUser = userRepository.getOrCreateUser(firebaseUser)

            // Geschäftslogik: Entscheidung über den AuthUiState basierend auf dem
            // de.geosphere.speechplaning.domain.usecase.auth.AppUser-Status
            if (appUser.approved) {
                AuthUiState.Authenticated(firebaseUser)
            } else {
                AuthUiState.NeedsApproval
            }
        } catch (e: Exception) {
            // Log.e("DetermineAppUserStatusUC", "Error determining app user status", e)
            AuthUiState.Unauthenticated
        }
    }
}
