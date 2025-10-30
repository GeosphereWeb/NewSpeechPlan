package de.geosphere.speechplaning.data.usecases

import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.data.authentication.AuthUiState
import de.geosphere.speechplaning.data.authentication.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

// Erstelle einen Dummy FirebaseUser für die Tests
fun createMockFirebaseUser(uid: String = "testUid", email: String = "test@example.com"): FirebaseUser {
    val mockUser = mockk<FirebaseUser>(relaxed = true)
    every { mockUser.uid } returns uid
    every { mockUser.email } returns email
    return mockUser
}

class DetermineAppUserStatusUseCaseTest : BehaviorSpec({

    lateinit var userRepository: UserRepository
    lateinit var useCase: DetermineAppUserStatusUseCase

    beforeEach {
        userRepository = mockk()
        useCase = DetermineAppUserStatusUseCase(userRepository)
    }

    given("determining app user status") {
        val mockFirebaseUser = createMockFirebaseUser()

        `when`("user is approved and no force reload") {
            then("it should return Authenticated state without reloading token") {
                val mockReloadAction = mockk<suspend (FirebaseUser) -> Unit>(relaxed = true)
                val approvedAppUser = AppUser(mockFirebaseUser.uid, approved = true)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns approvedAppUser

                val result =
                    useCase(firebaseUser = mockFirebaseUser, forceReload = false, reloadTokenAction = mockReloadAction)

                val authenticatedState = result.shouldBeInstanceOf<AuthUiState.Authenticated>()
                authenticatedState.firebaseUser shouldBe mockFirebaseUser

                coVerify(exactly = 1) { userRepository.getOrCreateUser(mockFirebaseUser) }
                coVerify(exactly = 0) { mockReloadAction.invoke(any()) } // Sollte nicht aufgerufen werden
            }
        }

        `when`("user is NOT approved and no force reload") {
            then("it should return NeedsApproval state without reloading token") {
                val mockReloadAction = mockk<suspend (FirebaseUser) -> Unit>(relaxed = true)
                val unapprovedAppUser = AppUser(mockFirebaseUser.uid, approved = false)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns unapprovedAppUser

                val result =
                    useCase(firebaseUser = mockFirebaseUser, forceReload = false, reloadTokenAction = mockReloadAction)

                result.shouldBeInstanceOf<AuthUiState.NeedsApproval>()
                coVerify(exactly = 1) { userRepository.getOrCreateUser(mockFirebaseUser) }
                coVerify(exactly = 0) { mockReloadAction.invoke(any()) } // Sollte nicht aufgerufen werden
            }
        }

        `when`("force reload is true and reload action is provided") {
            then("it should call reload token action and return Authenticated state") {
                val mockReloadAction = mockk<suspend (FirebaseUser) -> Unit>(relaxed = true)
                val approvedAppUser = AppUser(mockFirebaseUser.uid, approved = true)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns approvedAppUser

                val result =
                    useCase(firebaseUser = mockFirebaseUser, forceReload = true, reloadTokenAction = mockReloadAction)

                val authenticatedState = result.shouldBeInstanceOf<AuthUiState.Authenticated>()
                authenticatedState.firebaseUser shouldBe mockFirebaseUser

                coVerify(exactly = 1) { mockReloadAction.invoke(mockFirebaseUser) } // Sollte aufgerufen werden
                coVerify(exactly = 1) { userRepository.getOrCreateUser(mockFirebaseUser) }
            }
        }

        `when`("force reload is true but NO reload action is provided") {
            then("it should NOT call reload token action but still return Authenticated state") {
                val approvedAppUser = AppUser(mockFirebaseUser.uid, approved = true)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns approvedAppUser

                val result = useCase(firebaseUser = mockFirebaseUser, forceReload = true, reloadTokenAction = null)

                val authenticatedState = result.shouldBeInstanceOf<AuthUiState.Authenticated>()
                authenticatedState.firebaseUser shouldBe mockFirebaseUser

                coVerify(exactly = 1) { userRepository.getOrCreateUser(mockFirebaseUser) }
            }
        }

        `when`("getting app user from repository fails") {
            then("it should return Unauthenticated state") {
                val mockReloadAction = mockk<suspend (FirebaseUser) -> Unit>(relaxed = true)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } throws Exception("Database error")

                val result =
                    useCase(firebaseUser = mockFirebaseUser, forceReload = false, reloadTokenAction = mockReloadAction)

                result.shouldBeInstanceOf<AuthUiState.Unauthenticated>()
                coVerify(exactly = 1) { userRepository.getOrCreateUser(mockFirebaseUser) }
                // Kein Reload, da Fehler beim Abrufen des Users wichtiger ist
                coVerify(exactly = 0) { mockReloadAction.invoke(any()) }
            }
        }

        `when`("reloading token fails (even if user is approved)") {
            then("it should return Unauthenticated state") {
                val mockReloadAction = mockk<suspend (FirebaseUser) -> Unit>(relaxed = true)
                val approvedAppUser = AppUser(mockFirebaseUser.uid, approved = true)
                coEvery { userRepository.getOrCreateUser(mockFirebaseUser) } returns approvedAppUser
                coEvery { mockReloadAction.invoke(mockFirebaseUser) } throws Exception("Token reload failed")

                val result =
                    useCase(firebaseUser = mockFirebaseUser, forceReload = true, reloadTokenAction = mockReloadAction)

                result.shouldBeInstanceOf<AuthUiState.Unauthenticated>()
                coVerify(exactly = 1) { mockReloadAction.invoke(mockFirebaseUser) } // Reload wird versucht
                // getOrCreateUser wird in diesem Fall danach nicht aufgerufen, da der Reload scheitert
                coVerify(exactly = 0) { userRepository.getOrCreateUser(any()) }
            }
        }
    }
})
