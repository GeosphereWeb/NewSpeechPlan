package de.geosphere.speechplaning.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
import de.geosphere.speechplaning.ui.navigation.Screen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BottomNavigationItemTest {

    @Test
    fun `test BottomNavigationItem creation and properties`() {
        val testItem = BottomNavigationItem(
            label = "Test",
            selectedIcon = Icons.Default.CalendarMonth,
            unselectedIcon = Icons.Default.CalendarMonth,
            hasNews = true,
            route = Screen.PlaningRoute,
            badgeCount = 5
        )

        assertEquals("Test", testItem.label)
        assertEquals(Icons.Default.CalendarMonth, testItem.selectedIcon)
        assertEquals(Icons.Default.CalendarMonth, testItem.unselectedIcon)
        assertEquals(true, testItem.hasNews)
        assertEquals(Screen.PlaningRoute, testItem.route)
        assertEquals(5, testItem.badgeCount)
    }

    @Test
    fun `test companion object tabs list`() {
        val tabs = BottomNavigationItem.tabs
        assertEquals(3, tabs.size)

        // Test Plan tab
        val planTab = tabs[0]
        assertEquals("Plan", planTab.label)
        assertEquals(Icons.Filled.CalendarMonth, planTab.selectedIcon)
        assertEquals(Screen.PlaningRoute, planTab.route)
        assertEquals(true, planTab.hasNews)
        assertNull(planTab.badgeCount)


        // Test Speakers tab
        val speakersTab = tabs[1]
        assertEquals("Speakers", speakersTab.label)
        assertEquals(Icons.Filled.FolderShared, speakersTab.selectedIcon)
        assertEquals(Screen.SpeakerRoute, speakersTab.route)
        assertEquals(false, speakersTab.hasNews)
        assertNull(speakersTab.badgeCount)

        // Test Speeches tab
        val speechesTab = tabs[2]
        assertEquals("Speeches", speechesTab.label)
        assertEquals(Icons.AutoMirrored.Filled.ListAlt, speechesTab.selectedIcon)
        assertEquals(Screen.SpeechesRoute, speechesTab.route)
        assertEquals(false, speechesTab.hasNews)
        assertEquals(45, speechesTab.badgeCount)
    }

    @Test
    fun `test data class copy method`() {
        val originalItem = BottomNavigationItem(
            label = "Original",
            selectedIcon = Icons.Default.CalendarMonth,
            unselectedIcon = Icons.Default.CalendarMonth,
            hasNews = false,
            route = Screen.SpeakerRoute
        )
        val copiedItem = originalItem.copy(label = "Copied")

        assertEquals("Copied", copiedItem.label)
        assertEquals(originalItem.selectedIcon, copiedItem.selectedIcon)
        assertEquals(originalItem.route, copiedItem.route)
    }
}
