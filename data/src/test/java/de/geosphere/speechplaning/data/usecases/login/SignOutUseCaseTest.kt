package de.geosphere.speechplaning.data.usecases.login

import de.geosphere.speechplaning.data.authentication.AuthRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class SignOutUseCaseTest : BehaviorSpec({

    lateinit var authRepository: AuthRepository
    lateinit var useCase: SignOutUseCase

    beforeEach {
        // Erstellt für jeden Testfall einen frischen Mock
        authRepository = mockk(relaxed = true)
        useCase = SignOutUseCase(authRepository)
    }

    given("a user wants to sign out") {
        `when`("the sign out process is successful") {
            then("it should call both firebase sign out and clear credential manager") {
                // Act: Führe den Use Case aus
                val result = useCase()

                // Assert: Überprüfe, ob das Ergebnis erfolgreich ist
                result.shouldBeSuccess()

                // Assert: Überprüfe, ob beide Repository-Methoden genau einmal aufgerufen wurden
                coVerify(exactly = 1) { authRepository.signOutFirebase() }
                coVerify(exactly = 1) { authRepository.clearCredentialManager() }
            }
        }

        `when`("the firebase sign out fails") {
            then("it should return a failure and not clear credentials") {
                val exception = Exception("Firebase sign out failed")
                // Arrange: Simuliere einen Fehler bei der Firebase-Abmeldung
                coEvery { authRepository.signOutFirebase() } throws exception

                // Act: Führe den Use Case aus
                val result = useCase()

                // Assert: Das Ergebnis sollte ein Fehler sein
                result.shouldBeFailure { it shouldBe exception }

                // Assert: Nur die Firebase-Abmeldung wurde versucht
                coVerify(exactly = 1) { authRepository.signOutFirebase() }
                // Assert: Der Credential Manager wurde nicht angefasst, da der Prozess vorher abbrach
                coVerify(exactly = 0) { authRepository.clearCredentialManager() }
            }
        }

        `when`("clearing the credential manager fails") {
            then("it should return a failure but should have attempted firebase sign out") {
                val exception = Exception("CredentialManager clear failed")
                // Arrange: Simuliere einen Fehler nur beim CredentialManager
                coEvery { authRepository.clearCredentialManager() } throws exception

                // Act: Führe den Use Case aus
                val result = useCase()

                // Assert: Das Ergebnis sollte ein Fehler sein
                result.shouldBeFailure { it shouldBe exception }

                // Assert: Beide Methoden wurden versucht
                coVerify(exactly = 1) { authRepository.signOutFirebase() }
                coVerify(exactly = 1) { authRepository.clearCredentialManager() }
            }
        }
    }
})
