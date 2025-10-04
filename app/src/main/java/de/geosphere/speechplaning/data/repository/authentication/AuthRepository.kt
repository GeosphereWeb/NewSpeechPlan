package de.geosphere.speechplaning.data.repository.authentication

import kotlinx.coroutines.flow.StateFlow

/**
 * Ein zentrales Repository, das als "Single Source of Truth" für den anwendungsweiten
 * Authentifizierungs- und Autorisierungsstatus dient.
 */
interface AuthRepository {
    /**
     * Ein Flow, der den aktuellen, anwendungsweiten Authentifizierungsstatus bereitstellt.
     */
    val authUiState: StateFlow<AuthUiState>

    /**
     * Registriert einen neuen Benutzer mit E-Mail und Passwort.
     * Erstellt den Firebase Auth User und den zugehörigen AppUser-Eintrag in Firestore.
     * @return Gibt bei einem Fehler eine Exception zurück, ansonsten nichts (Unit).
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String)

    /**
     * Meldet einen bestehenden Benutzer mit E-Mail und Passwort an.
     * @return Gibt bei einem Fehler eine Exception zurück, ansonsten nichts (Unit).
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String)

    suspend fun forceReloadAndCheckUserStatus()

    /**
     * Meldet den aktuellen Benutzer ab.
     */
    fun signOut()
}
