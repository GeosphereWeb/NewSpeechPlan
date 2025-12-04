package de.geosphere.speechplaning.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderShared
import androidx.compose.ui.graphics.vector.ImageVector
import de.geosphere.speechplaning.core.navigation.Screen

data class BottomNavigationItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean,
    val route: Screen,
    val badgeCount: Int? = null,
) {
    companion object {
        val tabs = listOf(
            BottomNavigationItem(
                label = "Plan",
                selectedIcon = Icons.Filled.CalendarMonth,
                unselectedIcon = Icons.Outlined.CalendarMonth,
                route = Screen.PlaningRoute,
                hasNews = true,
            ),
            BottomNavigationItem(
                label = "Speakers",
                selectedIcon = Icons.Filled.FolderShared,
                unselectedIcon = Icons.Outlined.FolderShared,
                route = Screen.SpeakerRoute,
                hasNews = false,
            ),
            BottomNavigationItem(
                label = "Speeches",
                selectedIcon = Icons.AutoMirrored.Filled.ListAlt,
                unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt,
                hasNews = false,
                route = Screen.SpeechesRoute,
                badgeCount = 45
            ),
            BottomNavigationItem(
                label = "Districts",
                selectedIcon = Icons.AutoMirrored.Filled.ListAlt,
                unselectedIcon = Icons.AutoMirrored.Outlined.ListAlt,
                hasNews = false,
                route = Screen.DistrictsRoute,
                badgeCount = 1
            ),
        )
    }
}
