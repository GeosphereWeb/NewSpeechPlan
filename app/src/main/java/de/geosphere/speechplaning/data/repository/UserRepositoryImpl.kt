
package de.geosphere.speechplaning.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import de.geosphere.speechplaning.data.model.AppUser
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(private val firestore: FirebaseFirestore) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getOrCreateUser(firebaseUser: FirebaseUser): AppUser {
        val document = usersCollection.document(firebaseUser.uid)
        val snapshot = document[Source.SERVER].await()

        return if (snapshot.exists()) {
            // Manuelle und robuste Zuordnung, um "toObject()"-Fehler zu umgehen
            AppUser(
                uid = snapshot.id,
                email = snapshot.getString("email"),
                displayName = snapshot.getString("displayName"), // Liest den Namen, auch wenn er null ist
                approved = snapshot.getBoolean("approved") ?: false // Fällt auf false zurück, falls nicht vorhanden
            )
        } else {
            // Beim Erstellen eines neuen Nutzers wird `approved` explizit auf `false` gesetzt.
            val newUser = AppUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName, // Hier ist der displayName von Firebase Auth noch null
                approved = false
            )
            document.set(newUser).await()
            newUser
        }
    }
}
