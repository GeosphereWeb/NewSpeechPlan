package de.geosphere.speechplaning.data.authentication.permission

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.data.UserRole

@Suppress("ReturnCount")
class SpeakerPermissionPolicy {

    // Darf der User überhaupt neue Reden anlegen?
    fun canCreate(user: AppUser): Boolean {
        return user.role == UserRole.ADMIN
    }

    // Darf der User diese spezifische Rede bearbeiten?
    @Suppress("UnusedParameter")
    fun canEdit(user: AppUser, speaker: Speaker): Boolean {
        if (user.role == UserRole.ADMIN) return true
        // Beispiel für komplexere Logik (Autor darf eigene Rede bearbeiten)
        // if (user.uid == speech.authorId) return true
        return false
    }

    // Darf der User diese Rede löschen? (Vielleicht strenger als Edit?)
    @Suppress("UnusedParameter")
    fun canDelete(user: AppUser, speaker: Speaker): Boolean {
        // Nur Admins dürfen löschen
        if (user.role == UserRole.ADMIN) return true

        return false
    }

    // Hilfsmethode für globale UI-Sichtbarkeit (z.B. "Darf er generell Reden verwalten?")
    // Nützlich für Listenansichten, wo noch keine konkrete Rede gewählt ist
    fun canManageGeneral(user: AppUser): Boolean {
        return user.role == UserRole.ADMIN
    }
}
