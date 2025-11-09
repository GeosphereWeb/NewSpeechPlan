package de.geosphere.speechplaning.data.usecases

import android.util.Patterns
import de.geosphere.speechplaning.data.authentication.AuthRepository
class SignInWithEmailAndPasswordUseCase(
    private val authRepository: AuthRepository
) {
    /**
     * Meldet den Benutzer mit E-Mail und Passwort an, nachdem die Eingaben validiert wurden.
     *
     * @param email Die E-Mail-Adresse des Benutzers.
     * @param password Das Passwort des Benutzers.
     * @return Ein [Result], das Erfolg (mit [Unit]) oder Misserfolg (mit einer [Exception]) anzeigt.
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return runCatching {
            require(isEmailValid(email)) { "Die E-Mail-Adresse ist ungültig." }
            require(password.isNotBlank()) { "Das Passwort darf nicht leer sein." }

            // Dies ist eine Low-Level-Operation und bleibt im Repository
            authRepository.signInWithEmailAndPassword(email, password)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
