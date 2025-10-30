package de.geosphere.speechplaning.data.authentication

import com.google.firebase.auth.FirebaseUser

/**
 * Definiert die verschiedenen Zustände, in denen sich die Authentifizierungs-UI befinden kann.
 * Dieses Objekt repräsentiert einen anwendungsweiten Zustand und gehört daher zur Datenschicht.
 */
sealed interface AuthUiState {
    /**
     * Der Ladezustand, während der Auth-Status ermittelt wird.
     */
    data object Loading : AuthUiState

    /**
     * Repräsentiert einen erfolgreich angemeldeten und freigegebenen Nutzer.
     */
    data class Authenticated(val firebaseUser: FirebaseUser) : AuthUiState

    /**
     * Repräsentiert einen angemeldeten, aber noch nicht für die App-Nutzung freigegebenen Nutzer.
     */
    data object NeedsApproval : AuthUiState

    /**
     * Repräsentiert einen nicht angemeldeten Zustand.
     */
    data object Unauthenticated : AuthUiState
}
