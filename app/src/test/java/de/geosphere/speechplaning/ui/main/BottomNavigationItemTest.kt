package de.geosphere.speechplaning.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FolderShared
import de.geosphere.speechplaning.core.navigation.Screen
import de.geosphere.speechplaning.ui.BottomNavigationItem
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class BottomNavigationItemTest : ShouldSpec({

    should("have correct properties on creation") {
        val testItem = BottomNavigationItem(
            label = "Test",
            selectedIcon = Icons.Default.CalendarMonth,
            unselectedIcon = Icons.Default.CalendarMonth,
            hasNews = true,
            route = Screen.PlaningRoute,
            badgeCount = 5
        )

        testItem.label shouldBe "Test"
        testItem.selectedIcon shouldBe Icons.Default.CalendarMonth
        testItem.unselectedIcon shouldBe Icons.Default.CalendarMonth
        testItem.hasNews shouldBe true
        testItem.route shouldBe Screen.PlaningRoute
        testItem.badgeCount shouldBe 5
    }

    should("have correct tabs in companion object") {
        val tabs = BottomNavigationItem.tabs
        tabs.size shouldBe 3

        // Test Plan tab
        val planTab = tabs[0]
        planTab.label shouldBe "Plan"
        planTab.selectedIcon shouldBe Icons.Filled.CalendarMonth
        planTab.route shouldBe Screen.PlaningRoute
        planTab.hasNews shouldBe true
        planTab.badgeCount shouldBe null

        // Test Speakers tab
        val speakersTab = tabs[1]
        speakersTab.label shouldBe "Speakers"
        speakersTab.selectedIcon shouldBe Icons.Filled.FolderShared
        speakersTab.route shouldBe Screen.SpeakerRoute
        speakersTab.hasNews shouldBe false
        speakersTab.badgeCount shouldBe null

        // Test Speeches tab
        val speechesTab = tabs[2]
        speechesTab.label shouldBe "Speeches"
        speechesTab.selectedIcon shouldBe Icons.AutoMirrored.Filled.ListAlt
        speechesTab.route shouldBe Screen.SpeechesRoute
        speechesTab.hasNews shouldBe false
        speechesTab.badgeCount shouldBe 45
    }

    should("copy data class with new value") {
        val originalItem = BottomNavigationItem(
            label = "Original",
            selectedIcon = Icons.Default.CalendarMonth,
            unselectedIcon = Icons.Default.CalendarMonth,
            hasNews = false,
            route = Screen.SpeakerRoute
        )
        val copiedItem = originalItem.copy(label = "Copied")

        copiedItem.label shouldBe "Copied"
        copiedItem.selectedIcon shouldBe originalItem.selectedIcon
        copiedItem.route shouldBe originalItem.route
    }
})
