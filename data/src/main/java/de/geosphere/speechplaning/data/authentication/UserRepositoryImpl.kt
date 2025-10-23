package de.geosphere.speechplaning.data.authentication

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import de.geosphere.speechplaning.data.model.AppUser
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(private val firestore: FirebaseFirestore) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun getOrCreateUser(firebaseUser: FirebaseUser): AppUser {
        val document = usersCollection.document(firebaseUser.uid)
        // Wichtig: Source.SERVER erzwingt das Lesen vom Server und umgeht den Cache.
        val snapshot = document.get(Source.SERVER).await() // <--- PRÜFEN

        return if (snapshot.exists()) {
            snapshot.toObject(AppUser::class.java)!!
        } else {
            // Beim Erstellen eines neuen Nutzers wird `approved` explizit auf `false` gesetzt.
            // Das stellt sicher, dass das Feld im Firestore-Dokument immer existiert.
            val newUser = AppUser(
                uid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                approved = false
            )
            document.set(newUser).await()
            newUser
        }
    }
}
