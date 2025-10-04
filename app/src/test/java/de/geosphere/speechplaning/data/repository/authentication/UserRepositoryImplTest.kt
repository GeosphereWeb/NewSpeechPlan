package de.geosphere.speechplaning.data.repository.authentication

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import de.geosphere.speechplaning.data.model.AppUser
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class UserRepositoryImplTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var usersCollection: CollectionReference
    private lateinit var documentReference: DocumentReference
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        firestore = mockk()
        usersCollection = mockk()
        documentReference = mockk()
        every { firestore.collection("users") } returns usersCollection
        userRepository = UserRepositoryImpl(firestore)
    }

    @Test
    fun `getOrCreateUser returns existing user if document exists`() = runTest {
        // Given
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "test-uid"
            every { email } returns "test@example.com"
            every { displayName } returns "Test User"
        }
        val existingAppUser = AppUser(
            uid = "test-uid", email = "test@example.com",
            displayName = "Test User", approved = true
        )
        val documentSnapshot = mockk<DocumentSnapshot>()
        val getTask: Task<DocumentSnapshot> = Tasks.forResult(documentSnapshot)

        every { usersCollection.document(any()) } returns documentReference
        every { documentReference.get(Source.SERVER) } returns getTask
        every { documentSnapshot.exists() } returns true
        every { documentSnapshot.toObject(AppUser::class.java) } returns existingAppUser

        // When
        val result = userRepository.getOrCreateUser(firebaseUser)

        // Then
        assertEquals(existingAppUser, result)
        coVerify(exactly = 0) { documentReference.set(any()) }
    }

    @Test
    fun `getOrCreateUser creates and returns new user if document does not exist`() = runTest {
        // Given
        val firebaseUser = mockk<FirebaseUser> {
            every { uid } returns "new-user-uid"
            every { email } returns "new@example.com"
            every { displayName } returns "New User"
        }
        val documentSnapshot = mockk<DocumentSnapshot>()
        val getTask: Task<DocumentSnapshot> = Tasks.forResult(documentSnapshot)
        val setTask: Task<Void> = Tasks.forResult(null)

        every { usersCollection.document(any()) } returns documentReference
        every { documentReference.get(Source.SERVER) } returns getTask
        every { documentSnapshot.exists() } returns false
        every { documentReference.set(any()) } returns setTask

        // When
        val result = userRepository.getOrCreateUser(firebaseUser)

        // Then
        assertEquals("new-user-uid", result.uid)
        assertEquals("new@example.com", result.email)
        assertFalse(result.approved)

        coVerify { documentReference.set(any()) }
    }
}
