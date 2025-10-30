package de.geosphere.speechplaning.data.usecases

import de.geosphere.speechplaning.data.authentication.AuthRepository

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Meldet den aktuellen Benutzer von Firebase ab und löscht alle zugehörigen
     * Sitzungsdaten aus dem CredentialManager.
     *
     * @return Ein [Result], das Erfolg (mit [Unit]) oder Misserfolg (mit einer [Exception]) anzeigt.
     */
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            // Firebase-Abmeldung (Low-Level-Operation)
            authRepository.signOutFirebase()
            // CredentialManager-Status löschen (Low-Level-Operation, aber Teil des vollständigen Abmelde-Flusses)
            authRepository.clearCredentialManager()
        }
    }
}
