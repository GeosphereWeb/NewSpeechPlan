package de.geosphere.speechplaning.data.repository

import app.cash.turbine.test
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import de.geosphere.speechplaning.core.model.Speech
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class SpeechRepositoryTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val firestore = mockk<FirebaseFirestore>()
    val collection = mockk<CollectionReference>()
    val documentReference = mockk<DocumentReference>()
    val query = mockk<Query>()
    val querySnapshot = mockk<QuerySnapshot>()
    val registration = mockk<ListenerRegistration>(relaxed = true)

    // Dummy Tasks für await() Aufrufe
    val voidTask: Task<Void> = Tasks.forResult(null)
    val snapshotTask: Task<QuerySnapshot> = Tasks.forResult(querySnapshot)

    // Das Repository unter Test
    val repository = SpeechRepository(firestore)

    beforeTest {
        // Basis-Setup: Jedes Mal, wenn "speeches" angefragt wird, geben wir den Mock zurück
        every { firestore.collection("speeches") } returns collection
    }

    Given("getActiveSpeeches") {
        When("called") {
            // Mocking der Chain: collection -> whereEqualTo -> get -> await
            every { collection.whereEqualTo("active", true) } returns query
            every { query.get() } returns snapshotTask

            val activeSpeech = Speech(id = "1", number = "1", subject = "Active Subject", active = true)
            every { querySnapshot.toObjects(Speech::class.java) } returns listOf(activeSpeech)

            val result = repository.getActiveSpeeches()

            Then("it should return only active speeches") {
                result shouldContain activeSpeech
                verify { collection.whereEqualTo("active", true) }
            }
        }
    }

    Given("getAllSpeeches") {
        When("called") {
            // Mocking: collection -> get -> await
            every { collection.get() } returns snapshotTask

            val speech = Speech(id = "1", number = "1", subject = "Subject")
            every { querySnapshot.toObjects(Speech::class.java) } returns listOf(speech)

            val result = repository.getAllSpeeches()

            Then("it should return all speeches") {
                result shouldContain speech
                verify { collection.get() }
            }
        }
    }

    Given("saveSpeech") {
        When("called with a speech") {
            // Beispiel: Nummer ist 10 -> ID wird zu 10
            val speech = Speech(id = "", number = "10", subject = "Subject")

            // Erwartet: document("10") -> set(speech) -> await
            every { collection.document("10") } returns documentReference
            every { documentReference.set(any()) } returns voidTask

            repository.saveSpeech(speech)

            Then("it should save to firestore using the speech number as ID") {
                val slot = slot<Speech>()
                verify { documentReference.set(capture(slot)) }

                // Prüfen, ob die ID tatsächlich auf die Nummer gesetzt wurde (Logik in saveSpeech)
                slot.captured.id shouldBe "10"
                slot.captured.number shouldBe "10"
            }
        }
    }

    Given("deleteSpeech") {
        When("called with an ID") {
            val speechId = "123"

            // Erwartet: document("123") -> delete() -> await
            every { collection.document(speechId) } returns documentReference
            every { documentReference.delete() } returns voidTask

            repository.deleteSpeech(speechId)

            Then("it should delete the document from firestore") {
                verify { documentReference.delete() }
                verify { collection.document(speechId) }
            }
        }
    }

    Given("getAllSpeechesFlow") {
        When("listening to updates") {
            val listenerSlot = slot<EventListener<QuerySnapshot>>()

            // Mocking: addSnapshotListener -> gibt Registration zurück
            every { collection.addSnapshotListener(capture(listenerSlot)) } returns registration

            val speech = Speech(id = "FlowID", number = "99", subject = "Flow Subject")
            every { querySnapshot.toObjects(Speech::class.java) } returns listOf(speech)

            Then("it should emit updates when firestore notifies") {
                repository.getAllSpeechesFlow().test {
                    // 1. Listener wurde registriert (passiert beim collect)
                    verify { collection.addSnapshotListener(any()) }

                    // 2. Wir simulieren ein Event von Firestore
                    // snapshot != null, error == null
                    listenerSlot.captured.onEvent(querySnapshot, null)

                    // 3. Flow sollte die Liste emittieren
                    val result = awaitItem()
                    result shouldContain speech

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("firestore returns an error") {
            val listenerSlot = slot<EventListener<QuerySnapshot>>()
            every { collection.addSnapshotListener(capture(listenerSlot)) } returns registration

            Then("it should close the flow with exception") {
                repository.getAllSpeechesFlow().test {
                    // FIX: Exception mocken und Standard-Properties definieren
                    // Dies verhindert "no answer found for getCause()"
                    val firestoreException = mockk<FirebaseFirestoreException> {
                        every { message } returns "Firestore Error"
                        every { cause } returns null
                    }

                    // Simulieren eines Fehlers (snapshot null, error != null)
                    listenerSlot.captured.onEvent(null, firestoreException)

                    // Erwartet: Flow bricht mit Fehler ab
                    val error = awaitError()
                    error shouldBe firestoreException
                }
            }
        }
    }
})
