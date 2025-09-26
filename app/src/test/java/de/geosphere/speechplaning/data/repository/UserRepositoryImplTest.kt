package de.geosphere.speechplaning.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import de.geosphere.speechplaning.data.model.AppUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit-Tests für [UserRepositoryImpl].
 */
internal class UserRepositoryImplTest {

    // Mocks für die Firebase-Abhängigkeiten
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollectionRef: CollectionReference
    private lateinit var mockDocumentRef: DocumentReference
    private lateinit var mockDocumentSnapshot: DocumentSnapshot
    private lateinit var mockFirebaseUser: FirebaseUser

    // Die zu testende Klasse
    private lateinit var cut: UserRepositoryImpl

    // Testdaten
    private val testUserId = "test-uid"
    private val testUserEmail = "test@example.com"
    private val testUserDisplayName = "Test User"

    @BeforeEach
    fun setUp() {
        // Erstellen der Mocks vor jedem Test
        mockFirestore = mockk()
        mockCollectionRef = mockk()
        mockDocumentRef = mockk()
        mockDocumentSnapshot = mockk()
        mockFirebaseUser = mockk(relaxed = true) // relaxed = true, um nicht alle Properties mocken zu müssen

        // Standard-Setup für die Mock-Kette (Firestore -> Collection -> Document)
        every { mockFirestore.collection("users") } returns mockCollectionRef
        every { mockCollectionRef.document(any()) } returns mockDocumentRef

        // Standard-Setup für den FirebaseUser-Mock
        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns testUserEmail
        every { mockFirebaseUser.displayName } returns testUserDisplayName

        // Initialisieren des Repositorys mit den Mocks
        cut = UserRepositoryImpl(mockFirestore)
    }

    @Nested
    inner class GetOrCreateUser {

        @Test
        fun `should return existing user when document snapshot exists`() = runTest {
            // 1. Arrange: Konfiguriere das Verhalten für einen existierenden Nutzer
            val existingAppUser = AppUser(testUserId, testUserEmail, testUserDisplayName, approved = true)

            // Simulieren, dass der Snapshot existiert und das Nutzerobjekt zurückgibt
            every { mockDocumentSnapshot.exists() } returns true
            every { mockDocumentSnapshot.toObject(AppUser::class.java) } returns existingAppUser

            // KORREKTUR: Erstelle eine echte, bereits abgeschlossene Task, die den Snapshot enthält.
            val completedTask = Tasks.forResult(mockDocumentSnapshot)
            every { mockDocumentRef.get(Source.SERVER) } returns completedTask

            // 2. Act: Rufe die zu testende Methode auf
            val result = cut.getOrCreateUser(mockFirebaseUser)

            // 3. Assert: Überprüfe die Ergebnisse
            assertEquals(existingAppUser, result)

            // Stelle sicher, dass kein Schreibvorgang (.set) stattgefunden hat
            verify(exactly = 0) { mockDocumentRef.set(any()) }
        }

        @Test
        fun `should create and return new user when document snapshot does not exist`() = runTest {
            // 1. Arrange: Konfiguriere das Verhalten für einen neuen Nutzer

            // Simulieren, dass der Snapshot nicht existiert
            every { mockDocumentSnapshot.exists() } returns false

            // KORREKTUR: Erstelle eine bereits abgeschlossene Task, die den (leeren) Snapshot enthält.
            val completedGetTask = Tasks.forResult(mockDocumentSnapshot)
            every { mockDocumentRef.get(Source.SERVER) } returns completedGetTask

            // KORREKTUR: Erstelle eine bereits abgeschlossene Task für den Schreibvorgang.
            val completedSetTask = Tasks.forResult<Void>(null)
            every { mockDocumentRef.set(any()) } returns completedSetTask

            // 2. Act: Rufe die zu testende Methode auf
            val result = cut.getOrCreateUser(mockFirebaseUser)

            // 3. Assert: Überprüfe die Ergebnisse
            val expectedNewUser = AppUser(testUserId, testUserEmail, testUserDisplayName, approved = false)
            assertEquals(expectedNewUser, result)

            // Überprüfe, ob .set() mit dem korrekten Nutzerobjekt aufgerufen wurde
            val slot = slot<AppUser>()
            verify(exactly = 1) { mockDocumentRef.set(capture(slot)) }
            assertEquals(expectedNewUser, slot.captured)
        }
    }
}
