package de.geosphere.speechplaning.ui

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.data.UserRole
import de.geosphere.speechplaning.core.navigation.Screen
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

/**
 * Tests für die Tab-Filterung basierend auf User-Permissions.
 */
class BottomNavigationPermissionTest : ShouldSpec({

    should("show all tabs for ADMIN user") {
        val adminUser = AppUser(
            uid = "123",
            role = UserRole.ADMIN
        )

        val filteredTabs = filterTabsByPermissions(adminUser)

        filteredTabs.size shouldBe 5
        filteredTabs.any { it.route == Screen.PlaningRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeakerRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeechesRoute } shouldBe true
        filteredTabs.any { it.route == Screen.DistrictsRoute } shouldBe true
        filteredTabs.any { it.route == Screen.CongregationRoute } shouldBe true
    }

    should("show only Plan and Speeches tabs for SPEAKING_PLANER user") {
        val speakingPlanerUser = AppUser(
            uid = "456",
            role = UserRole.SPEAKING_PLANER
        )

        val filteredTabs = filterTabsByPermissions(speakingPlanerUser)

        filteredTabs.size shouldBe 2
        filteredTabs.any { it.route == Screen.PlaningRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeechesRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeakerRoute } shouldBe false
        filteredTabs.any { it.route == Screen.DistrictsRoute } shouldBe false
        filteredTabs.any { it.route == Screen.CongregationRoute } shouldBe false
    }

    should("show only Plan and Speeches tabs for SPEAKING_ASSISTANT user") {
        val speakingAssistantUser = AppUser(
            uid = "789",
            role = UserRole.SPEAKING_ASSISTANT
        )

        val filteredTabs = filterTabsByPermissions(speakingAssistantUser)

        filteredTabs.size shouldBe 2
        filteredTabs.any { it.route == Screen.PlaningRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeechesRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeakerRoute } shouldBe false
        filteredTabs.any { it.route == Screen.DistrictsRoute } shouldBe false
        filteredTabs.any { it.route == Screen.CongregationRoute } shouldBe false
    }

    should("show only Plan and Speeches tabs for NONE user") {
        val noneUser = AppUser(
            uid = "999",
            role = UserRole.NONE
        )

        val filteredTabs = filterTabsByPermissions(noneUser)

        filteredTabs.size shouldBe 2
        filteredTabs.any { it.route == Screen.PlaningRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeechesRoute } shouldBe true
        filteredTabs.any { it.route == Screen.SpeakerRoute } shouldBe false
        filteredTabs.any { it.route == Screen.DistrictsRoute } shouldBe false
        filteredTabs.any { it.route == Screen.CongregationRoute } shouldBe false
    }

    should("show all tabs when user is null") {
        val filteredTabs = filterTabsByPermissions(null)

        filteredTabs.size shouldBe 5
    }
})
