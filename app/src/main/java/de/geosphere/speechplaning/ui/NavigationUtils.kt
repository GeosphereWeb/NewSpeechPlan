package de.geosphere.speechplaning.ui

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.navigation.BottomNavigationItem
import de.geosphere.speechplaning.core.navigation.Screen

/**
 * Hilfsfunktionen für die Navigation.
 */

/**
 * Filtert Tab-Elemente basierend auf User-Permissions.
 * SPEAKING_PLANER und SPEAKING_ASSISTANT haben keinen Zugriff auf Congregation, District und Speaker.
 * Nur ADMIN hat Zugriff auf alle Tabs.
 *
 * @param currentUser Der aktuelle angemeldete User oder null, wenn kein User vorhanden ist
 * @return Eine gefilterte Liste von Navigationselementen
 */
fun filterTabsByPermissions(currentUser: AppUser?): List<BottomNavigationItem> {
    // Wenn kein User vorhanden, zeige alle Tabs (default Verhalten für Unauthenticated/Loading)
    if (currentUser == null) return BottomNavigationItem.Companion.tabs

    // Nur ADMIN hat Zugriff auf alle Tabs
    // SPEAKING_PLANER, SPEAKING_ASSISTANT und NONE dürfen nur Plan und Speeches sehen
    val isAdmin = currentUser.role.name == "ADMIN"
    return if (isAdmin) {
        BottomNavigationItem.Companion.tabs
    } else {
        BottomNavigationItem.Companion.tabs.filter { tab ->
            tab.route == Screen.PlaningRoute || tab.route == Screen.SpeechesRoute
        }
    }
}
