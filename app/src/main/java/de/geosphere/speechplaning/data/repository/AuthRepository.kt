package de.geosphere.speechplaning.data.repository

import de.geosphere.speechplaning.data.AuthUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * Ein zentrales Repository, das als "Single Source of Truth" für den anwendungsweiten
 * Authentifizierungs- und Autorisierungsstatus dient.
 */
interface AuthRepository {
    /**
     * Ein Flow, der den aktuellen UI-Zustand der Authentifizierung bereitstellt.
     * Die UI-Schicht (via ViewModel) kann diesen Flow beobachten, um auf Änderungen zu reagieren.
     */
    val authUiState: StateFlow<AuthUiState>
}
