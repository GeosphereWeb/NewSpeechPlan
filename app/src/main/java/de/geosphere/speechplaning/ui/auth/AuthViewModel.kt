package de.geosphere.speechplaning.ui.auth

import androidx.lifecycle.ViewModel
import de.geosphere.speechplaning.data.repository.AuthRepository

/**
 * Das ViewModel, das der UI den Authentifizierungsstatus bereitstellt.
 * Es enthält selbst keine Logik, sondern reicht den Zustand nur aus dem
 * anwendungsweiten [AuthRepository] durch.
 *
 * @param authRepository Das zentrale Repository für den Authentifizierungsstatus.
 */
class AuthViewModel(authRepository: AuthRepository) : ViewModel() {

    /**
     * Der UI-Zustand, der direkt vom anwendungsweiten AuthRepository stammt.
     * Die UI beobachtet diesen Flow, um auf Änderungen zu reagieren.
     */
    val uiState = authRepository.authUiState
}
