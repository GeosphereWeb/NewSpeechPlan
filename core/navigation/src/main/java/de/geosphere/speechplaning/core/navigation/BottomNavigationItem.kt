package de.geosphere.speechplaning.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.geosphere.speechplaning.theme.R

data class BottomNavigationItem(
    @field:StringRes val label: Int,
    @field:DrawableRes val selectedIcon: Int,
    @field:DrawableRes val unselectedIcon: Int,
    val hasNews: Boolean,
    val route: Screen,
    val badgeCount: Int? = null,
) {
    companion object {
        val tabs = listOf(
            BottomNavigationItem(
                label = R.string.planning_lb,
                selectedIcon = R.drawable.calendar_month,
                unselectedIcon = R.drawable.calendar_month,
                route = Screen.PlaningRoute,
                hasNews = true,
            ),
            BottomNavigationItem(
                label = R.string.speakers_lb,
                selectedIcon = R.drawable.interpreter,
                unselectedIcon = R.drawable.interpreter,
                route = Screen.SpeakerRoute,
                hasNews = false,
            ),
            BottomNavigationItem(
                label = R.string.speeches_lb,
                selectedIcon = R.drawable.speaker_notes,
                unselectedIcon = R.drawable.speaker_notes,
                hasNews = false,
                route = Screen.SpeechesRoute,
                badgeCount = null
            ),
            BottomNavigationItem(
                label = R.string.congregation_lb,
                selectedIcon = R.drawable.assembly_hall,
                unselectedIcon = R.drawable.assembly_hall,
                hasNews = false,
                route = Screen.CongregationRoute,
                badgeCount = null
            ),
            BottomNavigationItem(
                label = R.string.districts_lb,
                selectedIcon = R.drawable.apartment,
                unselectedIcon = R.drawable.apartment,
                hasNews = false,
                route = Screen.DistrictsRoute,
                badgeCount = null
            ),
        )
    }
}
