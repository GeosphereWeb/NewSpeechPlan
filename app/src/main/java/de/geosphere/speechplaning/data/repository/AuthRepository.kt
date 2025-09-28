package de.geosphere.speechplaning.data.repository

import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.AuthUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Ein zentrales Repository, das als "Single Source of Truth" für den anwendungsweiten
 * Authentifizierungs- und Autorisierungsstatus dient.
 */
interface AuthRepository {
    /**
     * Ein Flow, der den aktuellen UI-Zustand der Authentifizierung bereitstellt.
     */
    val authUiState: StateFlow<AuthUiState>

    /**
     * Prüft beim App-Start einmalig, ob bereits ein Nutzer angemeldet ist.
     */
    fun checkCurrentUser()

    /**
     * Verarbeitet das Ergebnis einer erfolgreichen Anmeldung.
     */
    suspend fun onSignInSuccess(user: FirebaseUser)

    /**
     * Meldet den aktuellen Benutzer ab.
     */
    fun signOut()
}
