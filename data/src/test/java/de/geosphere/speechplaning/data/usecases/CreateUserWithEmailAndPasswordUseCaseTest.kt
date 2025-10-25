package de.geosphere.speechplaning.data.usecases

// --- HIER IST DIE ÄNDERUNG ---
// Korrekter Import der zu testenden Klasse.
import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.model.repository.authentication.AuthRepository
import de.geosphere.speechplaning.data.model.repository.authentication.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.mockk

// ... der Rest der Test-Datei bleibt exakt gleich ...
class CreateUserWithEmailAndPasswordUseCaseTest : BehaviorSpec({

    // 1. Mocks für alle Abhängigkeiten erstellen
    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var useCase: CreateUserWithEmailAndPasswordUseCase

    // Dummy-Objekte für die Tests
    val mockFirebaseUser: FirebaseUser = mockk(relaxed = true)

    beforeEach {
        // Mocks vor jedem Test initialisieren
        authRepository = mockk()
        userRepository = mockk(relaxed = true) // relaxed, da getOrCreateUser Unit zurückgibt

        // Die zu testende Klasse mit den Mocks instanziieren
        useCase = CreateUserWithEmailAndPasswordUseCase(authRepository, userRepository)
    }

    given("a request to create a user with email and password") {

        val validEmail = "test@example.com"
        val validPassword = "password123"
        val validName = "Test User"

        `when`("all inputs are valid and repositories succeed") {
            then("it should call all repository methods in order and return success") {
                // Arrange (Vorbereiten): Definieren, was die Mocks tun sollen
                coEvery {
                    authRepository.createUserWithEmailAndPassword(validEmail, validPassword)
                } returns mockFirebaseUser

                coEvery {
                    authRepository.updateFirebaseUserProfile(mockFirebaseUser, validName)
                } returns Unit

                coEvery {
                    userRepository.getOrCreateUser(mockFirebaseUser)
                } returns mockk()

                // Act (Ausführen): Die zu testende Methode aufrufen
                val result = useCase(validEmail, validPassword, validName)

                // Assert (Überprüfen): Sicherstellen, dass das Ergebnis erfolgreich ist
                result.shouldBeSuccess()

                // Überprüfen, ob die Methoden in der richtigen Reihenfolge aufgerufen wurden
                coVerifyOrder {
                    authRepository.createUserWithEmailAndPassword(validEmail, validPassword)
                    authRepository.updateFirebaseUserProfile(mockFirebaseUser, validName)
                    userRepository.getOrCreateUser(mockFirebaseUser)
                }
            }
        }
        // ... alle anderen `when`-Blöcke bleiben unverändert
    }
})
