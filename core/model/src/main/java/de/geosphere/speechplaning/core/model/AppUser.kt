package de.geosphere.speechplaning.core.model

import com.google.firebase.firestore.DocumentId

/**
 * Repräsentiert die Daten eines Nutzers, wie sie in der "users"-Collection in Firestore gespeichert sind.
 *
 * @param uid Die einmalige ID des Nutzers von Firebase Authentication. Dient als Dokument-ID.
 * @param email Die E-Mail-Adresse des Nutzers.
 * @param displayName Der Anzeigename des Nutzers.
 * @param approved Ein Flag, das angibt, ob der Nutzer für die App-Nutzung freigeschaltet ist.
 */
data class AppUser(
    @DocumentId
    val uid: String = "",
    val email: String? = null,
    val displayName: String? = null,
    val approved: Boolean = false, // Name an Firestore-Schema angepasst
)
