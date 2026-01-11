package de.geosphere.speechplaning.ui

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.navigation.BottomNavigationItem
import de.geosphere.speechplaning.core.navigation.Screen

/**
 * Hilfsfunktionen für die Navigation.
 */

/**
 * Filtert Tab-Elemente basierend auf User-Permissions.
 * SPEAKING_PLANER hat keinen Zugriff auf Congregation, District und Speaker.
 *
 * @param currentUser Der aktuelle angemeldete User oder null, wenn kein User vorhanden ist
 * @return Eine gefilterte Liste von Navigationselementen
 */
fun filterTabsByPermissions(currentUser: AppUser?): List<BottomNavigationItem> {
    // Wenn kein User vorhanden, zeige alle Tabs (default Verhalten für Unauthenticated/Loading)
    if (currentUser == null) return BottomNavigationItem.Companion.tabs

    // SPEAKING_PLANER darf nur Plan und Speeches sehen
    val isSpeakingPlaner = currentUser.role.name == "SPEAKING_PLANER"
    return if (isSpeakingPlaner) {
        BottomNavigationItem.Companion.tabs.filter { tab ->
            tab.route == Screen.PlaningRoute || tab.route == Screen.SpeechesRoute
        }
    } else {
        // Admin und andere Rollen sehen alle Tabs
        BottomNavigationItem.Companion.tabs
    }
}
