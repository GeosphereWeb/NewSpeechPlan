package de.geosphere.speechplaning.data.usecases.login

import android.app.Activity
import android.os.Bundle
import androidx.credentials.Credential
import androidx.credentials.GetCredentialResponse
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import de.geosphere.speechplaning.data.authentication.AuthRepository
import de.geosphere.speechplaning.data.authentication.UserRepository
import de.geosphere.speechplaning.data.util.GoogleIdTokenParser
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll

class GoogleSignInUseCaseTest : BehaviorSpec({

    lateinit var authRepository: AuthRepository
    lateinit var userRepository: UserRepository
    lateinit var googleIdTokenParser: GoogleIdTokenParser // NEUER MOCK
    lateinit var useCase: GoogleSignInUseCase

    // Mocks für die Objekte
    val mockActivity: Activity = mockk()
    val mockGetCredentialResponse: GetCredentialResponse = mockk()
    val mockCredential: Credential = mockk()
    val mockBundle: Bundle = mockk() // Mock für das Bundle
    val mockFirebaseAuthCredential: AuthCredential = mockk()
    val mockFirebaseUser: FirebaseUser = mockk(relaxed = true)

    beforeEach {
        authRepository = mockk()
        userRepository = mockk(relaxed = true)
        googleIdTokenParser = mockk() // Initialisiere den neuen Mock
        useCase = GoogleSignInUseCase(authRepository, userRepository, googleIdTokenParser)

        // MockkStatic für GoogleAuthProvider wird weiterhin benötigt
        mockkStatic(GoogleAuthProvider::class)
    }

    afterEach {
        unmockkAll()
    }

    given("a Google Sign-In flow") {
        `when`("the entire process is successful") {
            then("it should complete all steps and return success") {
                // Arrange
                val mockToken = "mock_id_token"
                coEvery { authRepository.getGoogleIdCredential(mockActivity) } returns mockGetCredentialResponse
                every { mockGetCredentialResponse.credential } returns mockCredential
                every { mockCredential.data } returns mockBundle

                // Hier ist die entscheidende Änderung: Wir mocken unseren Parser!
                every { googleIdTokenParser.parseIdToken(mockBundle) } returns mockToken

                every { GoogleAuthProvider.getCredential(mockToken, null) } returns mockFirebaseAuthCredential
                coEvery { authRepository.signInWithFirebaseCredential(mockFirebaseAuthCredential) } returns mockk()
                coEvery { authRepository.getCurrentUser() } returns mockFirebaseUser

                // Act
                val result = useCase(mockActivity)

                // Assert
                result.shouldBeSuccess()

                // Verify
                coVerifyOrder {
                    authRepository.getGoogleIdCredential(mockActivity)
                    googleIdTokenParser.parseIdToken(mockBundle) // Verifiziere den Parser-Aufruf
                    GoogleAuthProvider.getCredential(mockToken, null)
                    authRepository.signInWithFirebaseCredential(mockFirebaseAuthCredential)
                    authRepository.getCurrentUser()
                    userRepository.getOrCreateUser(mockFirebaseUser)
                }
            }
        }

        `when`("getting the Google credential fails") {
            // ... (Dieser Testfall bleibt unverändert)
            then("it should return a failure and stop immediately") {
                val exception = Exception("Credential fetch failed")
                coEvery { authRepository.getGoogleIdCredential(mockActivity) } throws exception
                val result = useCase(mockActivity)
                result.shouldBeFailure(exception)
                coVerify(exactly = 0) { googleIdTokenParser.parseIdToken(any()) }
                coVerify(exactly = 0) { authRepository.signInWithFirebaseCredential(any()) }
            }
        }

        `when`("the user is null after a successful Firebase sign-in") {
            // ... (Dieser Testfall bleibt fast unverändert)
            then("it should throw an IllegalStateException") {
                val mockToken = "mock_id_token"
                coEvery { authRepository.getGoogleIdCredential(mockActivity) } returns mockGetCredentialResponse
                every { mockGetCredentialResponse.credential } returns mockCredential
                every { mockCredential.data } returns mockBundle
                every { googleIdTokenParser.parseIdToken(mockBundle) } returns mockToken
                every { GoogleAuthProvider.getCredential(mockToken, null) } returns mockFirebaseAuthCredential
                coEvery { authRepository.signInWithFirebaseCredential(mockFirebaseAuthCredential) } returns mockk()
                coEvery { authRepository.getCurrentUser() } returns null // Fehlerpunkt

                val result = useCase(mockActivity)

                val exception = result.shouldBeFailure<IllegalStateException>()
                exception shouldHaveMessage "User not found after successful Google sign-in."
                coVerify(exactly = 0) { userRepository.getOrCreateUser(any()) }
            }
        }
    }
})
