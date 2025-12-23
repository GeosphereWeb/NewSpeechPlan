package de.geosphere.speechplaning.data.authentication.permission

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.UserRole

@Suppress("ReturnCount")
class CongregationEventPermissionPolicy {

    // Darf der User überhaupt neue Reden anlegen?
    fun canCreate(user: AppUser): Boolean {
        return user.role == UserRole.ADMIN || user.role == UserRole.SPEAKING_PLANER
    }

    // Darf der User diese spezifische Rede bearbeiten?
    @Suppress("UnusedParameter")
    fun canEdit(user: AppUser, congregationEvent: CongregationEvent): Boolean {
        if (user.role == UserRole.ADMIN) return true
        if (user.role == UserRole.SPEAKING_PLANER) return true

        // Beispiel für komplexere Logik (Autor darf eigene Rede bearbeiten)
        // if (user.uid == speech.authorId) return true

        return false
    }

    // Darf der User diese Rede löschen? (Vielleicht strenger als Edit?)
    @Suppress("UnusedParameter")
    fun canDelete(user: AppUser, congregationEvent: CongregationEvent): Boolean {
        // Nur Admins dürfen löschen
        if (user.role == UserRole.ADMIN) return true

        // Planer dürfen vielleicht löschen, Autoren aber nicht
        if (user.role == UserRole.SPEAKING_PLANER) return true

        return false
    }

    // Hilfsmethode für globale UI-Sichtbarkeit (z.B. "Darf er generell Reden verwalten?")
    // Nützlich für Listenansichten, wo noch keine konkrete Rede gewählt ist
    fun canManageGeneral(user: AppUser): Boolean {
        return user.role == UserRole.ADMIN || user.role == UserRole.SPEAKING_PLANER
    }
}
