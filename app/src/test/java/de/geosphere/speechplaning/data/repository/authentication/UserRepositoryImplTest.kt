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
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk

class UserRepositoryImplTest : BehaviorSpec({

    lateinit var firestore: FirebaseFirestore
    lateinit var usersCollection: CollectionReference
    lateinit var documentReference: DocumentReference
    lateinit var userRepository: UserRepository

    beforeEach { 
        firestore = mockk()
        usersCollection = mockk()
        documentReference = mockk()
        every { firestore.collection("users") } returns usersCollection
        userRepository = UserRepositoryImpl(firestore)
    }

    given("getOrCreateUser") {
        `when`("document exists") {
            then("it should return existing user") {
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
                result shouldBe existingAppUser
                coVerify(exactly = 0) { documentReference.set(any()) }
            }
        }

        `when`("document does not exist") {
            then("it should create and return new user") {
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
                result.uid shouldBe "new-user-uid"
                result.email shouldBe "new@example.com"
                result.approved.shouldBeFalse()

                coVerify { documentReference.set(any()) }
            }
        }
    }
})
