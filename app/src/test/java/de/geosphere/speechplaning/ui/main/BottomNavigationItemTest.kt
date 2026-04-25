package de.geosphere.speechplaning.ui.main

import de.geosphere.speechplaning.core.navigation.BottomNavigationItem
import de.geosphere.speechplaning.core.navigation.Screen
import de.geosphere.speechplaning.theme.R
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class BottomNavigationItemTest : ShouldSpec({

    should("have correct properties on creation") {
        val testItem = BottomNavigationItem(
            label = R.string.planning_lb,
            selectedIcon = R.drawable.calendar_month,
            unselectedIcon = R.drawable.calendar_month,
            hasNews = true,
            route = Screen.PlaningRoute,
            badgeCount = 5
        )

        testItem.label shouldBe R.string.planning_lb
        testItem.selectedIcon shouldBe R.drawable.calendar_month
        testItem.unselectedIcon shouldBe R.drawable.calendar_month
        testItem.hasNews shouldBe true
        testItem.route shouldBe Screen.PlaningRoute
        testItem.badgeCount shouldBe 5
    }

    should("have correct tabs in companion object") {
        val tabs = BottomNavigationItem.tabs
        tabs.size shouldBe 5

        // Test Plan tab
        val planTab = tabs[0]
        planTab.label shouldBe R.string.planning_lb
        planTab.selectedIcon shouldBe R.drawable.calendar_month
        planTab.route shouldBe Screen.PlaningRoute
        planTab.hasNews shouldBe true
        planTab.badgeCount shouldBe null

        // Test Speakers tab
        val speakersTab = tabs[1]
        speakersTab.label shouldBe R.string.speakers_lb
        speakersTab.selectedIcon shouldBe R.drawable.interpreter
        speakersTab.route shouldBe Screen.SpeakerRoute
        speakersTab.hasNews shouldBe false
        speakersTab.badgeCount shouldBe null

        // Test Speeches tab
        val speechesTab = tabs[2]
        speechesTab.label shouldBe R.string.speeches_lb
        speechesTab.selectedIcon shouldBe R.drawable.speaker_notes
        speechesTab.route shouldBe Screen.SpeechesRoute
        speechesTab.hasNews shouldBe false
        speechesTab.badgeCount shouldBe null

        // Test Congregation tab
        val congregationTab = tabs[3]
        congregationTab.label shouldBe R.string.congregation_lb
        congregationTab.selectedIcon shouldBe R.drawable.warehouse
        congregationTab.route shouldBe Screen.CongregationRoute
        congregationTab.hasNews shouldBe false
        congregationTab.badgeCount shouldBe null

        // Test Districts tab
        val districtsTab = tabs[4]
        districtsTab.label shouldBe R.string.districts_lb
        districtsTab.selectedIcon shouldBe R.drawable.apartment
        districtsTab.route shouldBe Screen.DistrictsRoute
        districtsTab.hasNews shouldBe false
        districtsTab.badgeCount shouldBe null
    }

    should("copy data class with new value") {
        val originalItem = BottomNavigationItem(
            label = R.string.planning_lb,
            selectedIcon = R.drawable.calendar_month,
            unselectedIcon = R.drawable.calendar_month,
            hasNews = false,
            route = Screen.SpeakerRoute
        )
        val copiedItem = originalItem.copy(label = R.string.speakers_lb)

        copiedItem.label shouldBe R.string.speakers_lb
        copiedItem.selectedIcon shouldBe originalItem.selectedIcon
        copiedItem.route shouldBe originalItem.route
    }
})
