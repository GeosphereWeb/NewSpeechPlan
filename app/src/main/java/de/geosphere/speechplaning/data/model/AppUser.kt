package de.geosphere.speechplaning.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Repräsentiert die Daten eines Nutzers, wie sie in der "users"-Collection in Firestore gespeichert sind.
 *
 * WICHTIG: Die @PropertyName-Annotationen sind entscheidend, damit Firestore die Felder auch dann
 * korrekt zuordnen kann, wenn sie in der Datenbank null sind oder wenn ProGuard/R8 die Namen ändert.
 */
data class AppUser(
    @DocumentId
    val uid: String = "",

    @get:PropertyName("email")
    val email: String? = null,

    @get:PropertyName("displayName")
    val displayName: String? = null,

    @get:PropertyName("approved")
    val approved: Boolean = false,
)
