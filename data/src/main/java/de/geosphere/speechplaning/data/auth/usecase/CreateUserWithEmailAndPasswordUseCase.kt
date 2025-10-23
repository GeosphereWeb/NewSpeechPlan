package de.geosphere.speechplaning.data.auth.usecase

import de.geosphere.speechplaning.data.authentication.AuthRepository
import de.geosphere.speechplaning.data.authentication.UserRepository

// Android-spezifischen Import entfernen
// import android.util.Patterns

private const val MIN_CHAR = 6

class CreateUserWithEmailAndPasswordUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    /**
     * Erstellt einen neuen Benutzer mit E-Mail und Passwort nach Validierung der Eingaben.
     * ...
     */
    suspend operator fun invoke(email: String, password: String, name: String): Result<Unit> {
        return runCatching {
            require(name.isNotBlank()) { "Der Name darf nicht leer sein." }
            require(isEmailValid(email)) { "Die E-Mail-Adresse ist ungültig." }
            require(password.length >= MIN_CHAR) { "Das Passwort muss mindestens 6 Zeichen lang sein." }

            // 1. Firebase Auth Benutzer erstellen (Low-Level-Operation über AuthRepository)
            val firebaseUser = authRepository.createUserWithEmailAndPassword(email, password)

            // 2. Anzeigenamen des Firebase-Benutzerprofils aktualisieren (Geschäftslogik im UseCase)
            authRepository.updateFirebaseUserProfile(firebaseUser, name)

            // 3. Den zugehörigen AppUser-Eintrag in Firestore/DB erstellen (Geschäftslogik im UseCase)
            userRepository.getOrCreateUser(firebaseUser)
        }
    }

    /**
     *  --- HIER IST DIE ÄNDERUNG ---
     * Validiert die E-Mail-Adresse mit einer reinen Kotlin-Regex-Implementierung,
     * um die Abhängigkeit vom Android-Framework zu entfernen.
     */
    private fun isEmailValid(email: String): Boolean {
        // Eine einfache, aber für die meisten Fälle ausreichende Regex.
        // Sie ist nicht so perfekt wie android.util.Patterns, aber für Unit-Tests und
        // die Domain-Schicht ist diese Entkopplung wichtiger.
        return "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex().matches(email)
    }
}
