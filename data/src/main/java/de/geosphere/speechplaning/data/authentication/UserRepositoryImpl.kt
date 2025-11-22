package de.geosphere.speechplaning.data.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source.SERVER
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.data.UserRole
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getOrCreateUser(firebaseUser: FirebaseUser): AppUser {
        val document = usersCollection.document(firebaseUser.uid)
        // Wichtig: Source.SERVER erzwingt das Lesen vom Server und umgeht den Cache.
        val snapshot = document.get(SERVER).await() // <--- PRÜFEN

        return if (snapshot.exists()) {
            snapshot.toObject(AppUser::class.java)!!
        } else {
            // Beim Erstellen eines neuen Nutzers wird `approved` explizit auf `false` gesetzt.
            // Das stellt sicher, dass das Feld im Firestore-Dokument immer existiert.
            val newUser = AppUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                approved = false,
                role = UserRole.SPEAKING_ASSISTANT
            )
            document.set(newUser).await()
            newUser
        }
    }

    // /**
    //  * Fügt einem Nutzer eine Rolle hinzu und stellt sicher, dass keine Duplikate entstehen.
    //  * @param userId Die UID des Nutzers, der aktualisiert werden soll.
    //  * @param roleToAdd Die Rolle, die hinzugefügt werden soll.
    //  */
    // @Suppress("UseCheckOrError")
    // override suspend fun addRoleToUser(userId: String, roleToAdd: UserRole) {
    //     // 1. Lade den aktuellen Zustand des Nutzers vom Server.
    //     val currentUser = getUser(userId) ?: throw IllegalStateException("Benutzer mit ID $userId nicht gefunden.")
    //
    //     // 2. Prüfe, ob die Rolle bereits vorhanden ist.
    //     if (currentUser.role.contains(roleToAdd)) {
    //         println("Rolle '$roleToAdd' ist bereits zugewiesen. Überspringe Update.")
    //         return // Beende die Funktion, da nichts zu tun ist.
    //     }
    //
    //     // 3. Wenn nicht vorhanden: Erstelle eine neue Liste.
    //     // Die "idiomatische" Kotlin-Variante mit Umwandlung zu Set und zurück ist hier sehr elegant.
    //     val updatedRoles = (currentUser.role.toSet() + roleToAdd).toList()
    //
    //     // 4. Erstelle eine Kopie des Nutzer-Objekts mit der aktualisierten Rollenliste.
    //     val updatedUser = currentUser.copy(role = updatedRoles)
    //
    //     // 5. Speichere das vollständige, aktualisierte Nutzer-Objekt zurück in Firestore.
    //     updateUser(updatedUser)
    // }

    /**
     * Ruft einen Nutzer anhand seiner UID ab.
     * @return Den AppUser oder null, wenn nicht gefunden.
     */
    override suspend fun getUser(userId: String): AppUser? {
        return try {
            usersCollection.document(userId)
                .get(SERVER)
                .await()
                .toObject(AppUser::class.java)
        } catch (e: Exception) {
            // Logge den Fehler oder wirf ihn weiter
            println("Fehler beim Abrufen des Nutzers $userId: ${e.message}")
            null
        }
    }

    /**
     * Aktualisiert ein gesamtes Nutzerdokument in Firestore.
     */
    override suspend fun updateUser(user: AppUser) {
        usersCollection.document(user.uid).set(user).await()
    }

    override val currentUser: Flow<AppUser?> = callbackFlow {
        // 1. Wir hören auf den Login-Status von Firebase Auth
        val authListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(authListener)

        awaitClose {
            firebaseAuth.removeAuthStateListener(authListener)
        }
    }.flatMapLatest { firebaseUser ->
        // 2. Reagieren auf Änderungen im Auth-Status

        if (firebaseUser == null) {
            // A) Kein User eingeloggt -> Wir emittieren null
            flowOf(null)
        } else {
            // B) User ist eingeloggt -> Wir abonnieren das Firestore-Dokument
            callbackFlow {
                val registration = usersCollection.document(firebaseUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // Optional: Error loggen
                            close(error)
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            // Mapping von Firestore Document -> AppUser Object
                            val user = snapshot.toObject(AppUser::class.java)
                            trySend(user)
                        } else {
                            // Dokument existiert (noch) nicht
                            trySend(null)
                        }
                    }

                // Aufräumen, wenn der User sich ausloggt oder der Flow cancelled wird
                awaitClose {
                    registration.remove()
                }
            }
        }
    }
}
