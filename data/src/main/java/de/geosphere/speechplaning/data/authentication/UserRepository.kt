package de.geosphere.speechplaning.data.authentication

import com.google.firebase.auth.FirebaseUser
import de.geosphere.speechplaning.core.model.AppUser
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /**
     * Ruft einen de.geosphere.speechplaning.domain.usecase.auth.AppUser aus Firestore ab oder erstellt einen neuen,
     * falls keiner existiert.
     *
     * @param firebaseUser Der aktuell angemeldete FirebaseUser.
     * @return Der de.geosphere.speechplaning.domain.usecase.auth.AppUser aus Firestore.
     */
    suspend fun getOrCreateUser(firebaseUser: FirebaseUser): AppUser

    // suspend fun addRoleToUser(userId: String, roleToAdd: UserRole)
    suspend fun getUser(userId: String): AppUser?
    suspend fun updateUser(user: AppUser)

    val currentUser: Flow<AppUser?>
}
