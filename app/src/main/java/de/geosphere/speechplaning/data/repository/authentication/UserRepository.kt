package de.geosphere.speechplaning.data.repository.authentication

import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.data.model.AppUser

fun interface UserRepository {
    /**
     * Ruft einen AppUser aus Firestore ab oder erstellt einen neuen, falls keiner existiert.
     *
     * @param firebaseUser Der aktuell angemeldete FirebaseUser.
     * @return Der AppUser aus Firestore.
     */
    suspend fun getOrCreateUser(firebaseUser: FirebaseUser): AppUser
}
