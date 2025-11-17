package de.geosphere.speechplaning.data.authentication

import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.data.UserRole
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.tasks.await

class UserRepositoryImplTest : BehaviorSpec({

    // Mocks
    val firestore = mockk<FirebaseFirestore>()
    val firebaseAuth = mockk<FirebaseAuth>()
    val usersCollection = mockk<CollectionReference>()
    val documentReference = mockk<DocumentReference>()
    val firebaseUser = mockk<FirebaseUser>()

    // Task Mocks für Coroutines await()
    val documentSnapshotTask = mockk<Task<DocumentSnapshot>>()
    val voidTask = mockk<Task<Void>>()
    val documentSnapshot = mockk<DocumentSnapshot>()
    val listenerRegistration = mockk<ListenerRegistration>()

    lateinit var repository: UserRepositoryImpl

    // Slots zum Fangen von Listenern
    val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()
    val snapshotListenerSlot = slot<EventListener<DocumentSnapshot>>()

    // Setup static mocking für .await() Extension Function
    beforeSpec {
        mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    afterSpec {
        unmockkStatic("kotlinx.coroutines.tasks.TasksKt")
    }

    beforeTest {
        // Standard Mocks Setup
        every { firestore.collection("users") } returns usersCollection
        every { usersCollection.document(any()) } returns documentReference

        // Listener Registration Mock (für remove() Aufrufe)
        every { listenerRegistration.remove() } returns Unit

        repository = UserRepositoryImpl(firestore, firebaseAuth)
    }

    Given("getOrCreateUser") {
        val uid = "test_uid_123"
        every { firebaseUser.uid } returns uid
        every { firebaseUser.email } returns "test@example.com"
        every { firebaseUser.displayName } returns "Max Mustermann"

        When("User already exists in Firestore") {
            every { documentReference.get(Source.SERVER) } returns documentSnapshotTask
            coEvery { documentSnapshotTask.await() } returns documentSnapshot
            every { documentSnapshot.exists() } returns true

            val existingUser = AppUser(uid = uid, displayName = "Existing User", role = UserRole.ADMIN)
            every { documentSnapshot.toObject(AppUser::class.java) } returns existingUser

            val result = repository.getOrCreateUser(firebaseUser)

            Then("return the existing user") {
                result shouldBe existingUser
            }

            Then("do not write to database") {
                coVerify(exactly = 0) { documentReference.set(any()) }
            }
        }

        When("User does not exist in Firestore") {
            every { documentReference.get(Source.SERVER) } returns documentSnapshotTask
            coEvery { documentSnapshotTask.await() } returns documentSnapshot
            every { documentSnapshot.exists() } returns false

            // Mock für das Schreiben des neuen Users
            every { documentReference.set(any()) } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            val result = repository.getOrCreateUser(firebaseUser)

            Then("return a new user with default role SPEAKING_ASSISTANT and approved=false") {
                result.uid shouldBe uid
                result.role shouldBe UserRole.SPEAKING_ASSISTANT
                result.approved shouldBe false
                result.email shouldBe "test@example.com"
            }

            Then("save the new user to Firestore") {
                coVerify(exactly = 1) { documentReference.set(any()) }
            }
        }
    }

    Given("getUser") {
        val uid = "some_uid"

        When("Firestore returns a document") {
            every { usersCollection.document(uid) } returns documentReference
            every { documentReference.get(Source.SERVER) } returns documentSnapshotTask
            coEvery { documentSnapshotTask.await() } returns documentSnapshot

            val appUser = AppUser(uid = uid, displayName = "Found Me")
            every { documentSnapshot.toObject(AppUser::class.java) } returns appUser

            val result = repository.getUser(uid)

            Then("return the mapped AppUser") {
                result shouldBe appUser
            }
        }

        When("Firestore throws an exception") {
            every { usersCollection.document(uid) } returns documentReference
            every { documentReference.get(Source.SERVER) } returns documentSnapshotTask
            coEvery { documentSnapshotTask.await() } throws RuntimeException("Connection failed")

            val result = repository.getUser(uid)

            Then("return null and catch exception") {
                result.shouldBeNull()
            }
        }
    }

    Given("updateUser") {
        When("called with a user") {
            val userToUpdate = AppUser(uid = "update_id", displayName = "Updated Name")
            every { usersCollection.document(userToUpdate.uid) } returns documentReference
            every { documentReference.set(userToUpdate) } returns voidTask
            coEvery { voidTask.await() } returns mockk()

            repository.updateUser(userToUpdate)

            Then("call set on the document") {
                coVerify { documentReference.set(userToUpdate) }
            }
        }
    }

    Given("currentUser Flow") {

        When("User is logged out (null)") {
            every { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) } returns Unit
            every { firebaseAuth.removeAuthStateListener(any()) } returns Unit

            Then("emit null") {
                repository.currentUser.test {
                    // Trigger Auth Listener with null
                    authStateListenerSlot.captured.onAuthStateChanged(mockk { every { currentUser } returns null })

                    awaitItem().shouldBeNull()
                }
            }
        }

        When("User is logged in and Firestore document exists") {
            val uid = "login_uid"
            val mockAuth = mockk<FirebaseAuth>()
            val mockFirebaseUser = mockk<FirebaseUser>()
            every { mockAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.uid } returns uid

            every { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) } returns Unit
            every { firebaseAuth.removeAuthStateListener(any()) } returns Unit

            // Setup Snapshot Listener Mock
            every { usersCollection.document(uid) } returns documentReference
            every { documentReference.addSnapshotListener(capture(snapshotListenerSlot)) } returns listenerRegistration

            val dbUser = AppUser(uid = uid, displayName = "DB User")
            every { documentSnapshot.exists() } returns true
            every { documentSnapshot.toObject(AppUser::class.java) } returns dbUser

            Then("emit the user from Firestore") {
                repository.currentUser.test {
                    // 1. Auth Status ändert sich zu "eingeloggt"
                    authStateListenerSlot.captured.onAuthStateChanged(mockAuth)

                    // 2. Snapshot Listener feuert mit Daten
                    snapshotListenerSlot.captured.onEvent(documentSnapshot, null)

                    awaitItem() shouldBe dbUser
                }
            }
        }

        When("User is logged in but Firestore document does NOT exist yet") {
            val uid = "login_uid_missing"
            val mockAuth = mockk<FirebaseAuth>()
            val mockFirebaseUser = mockk<FirebaseUser>()
            every { mockAuth.currentUser } returns mockFirebaseUser
            every { mockFirebaseUser.uid } returns uid

            every { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) } returns Unit
            every { firebaseAuth.removeAuthStateListener(any()) } returns Unit

            every { usersCollection.document(uid) } returns documentReference
            every { documentReference.addSnapshotListener(capture(snapshotListenerSlot)) } returns listenerRegistration

            every { documentSnapshot.exists() } returns false

            Then("emit null (or handle gracefully)") {
                repository.currentUser.test {
                    authStateListenerSlot.captured.onAuthStateChanged(mockAuth)
                    snapshotListenerSlot.captured.onEvent(documentSnapshot, null)

                    // Im Code steht: if (exists) emit(user) else trySend(null)
                    awaitItem().shouldBeNull()
                }
            }
        }
    }
})
