package de.geosphere.speechplaning.data.authentication.permission

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.UserRole

class CongregationEventPermissionPolicy {

    // Darf der User überhaupt neue Reden anlegen?
    fun canCreate(user: AppUser): Boolean {
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.SPEAKING_PLANER -> true
            else -> false
        }
    }

    // Darf der User diese spezifische Rede bearbeiten?
    @Suppress("UnusedParameter")
    fun canEdit(user: AppUser, congregationEvent: CongregationEvent): Boolean {
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.SPEAKING_PLANER -> true
            UserRole.SPEAKING_ASSISTANT -> true
            // Beispiel für komplexere Logik (Autor darf eigene Rede bearbeiten)
            // if (user.uid == speech.authorId) return true
            else -> false
        }
    }

    // Darf der User diese Rede löschen? (Vielleicht strenger als Edit?)
    @Suppress("UnusedParameter")
    fun canDelete(user: AppUser, congregationEvent: CongregationEvent): Boolean {
        // Nur Admins dürfen löschen
        // Planer dürfen vielleicht löschen, Autoren aber nicht
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.SPEAKING_PLANER -> true
            else -> false
        }
    }

    // Hilfsmethode für globale UI-Sichtbarkeit (z.B. "Darf er generell Reden verwalten?")
    // Nützlich für Listenansichten, wo noch keine konkrete Rede gewählt ist
    fun canManageGeneral(user: AppUser): Boolean {
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.SPEAKING_PLANER -> true
            else -> false
        }
    }

    // Darf der User den "Redner ist informiert" Status ändern?
    // SPEAKING_ASSISTANT darf diesen Switch betätigen
    fun canToggleSpeakerInformed(user: AppUser): Boolean {
        return when (user.role) {
            UserRole.ADMIN -> true
            UserRole.SPEAKING_PLANER -> true
            UserRole.SPEAKING_ASSISTANT -> true
            else -> false
        }
    }
}
