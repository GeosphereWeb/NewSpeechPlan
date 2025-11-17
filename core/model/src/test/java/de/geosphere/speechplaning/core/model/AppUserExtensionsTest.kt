package de.geosphere.speechplaning.core.model

import de.geosphere.speechplaning.core.model.data.UserRole
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class AppUserExtensionsTest : BehaviorSpec({

    Given("An AppUser with role ADMIN") {
        val adminUser = AppUser(
            uid = "1",
            email = "admin@test.com",
            displayName = "Admin",
            role = UserRole.ADMIN
        )

        Then("canEditSpeeches should return true") {
            adminUser.canEditSpeeches().shouldBeTrue()
        }

        Then("canManageUsers should return true") {
            adminUser.canManageUsers().shouldBeTrue()
        }

        Then("canViewInternalNotes should return true") {
            adminUser.canViewInternalNotes().shouldBeTrue()
        }
    }

    Given("An AppUser with role SPEAKING_PLANER") {
        val plannerUser = AppUser(
            uid = "2",
            email = "planner@test.com",
            displayName = "Planner",
            role = UserRole.SPEAKING_PLANER
        )

        Then("canEditSpeeches should return true") {
            plannerUser.canEditSpeeches().shouldBeTrue()
        }

        Then("canManageUsers should return true") {
            plannerUser.canManageUsers().shouldBeTrue()
        }

        Then("canViewInternalNotes should return true") {
            plannerUser.canViewInternalNotes().shouldBeTrue()
        }
    }

    Given("An AppUser with role SPEAKING_ASSISTANT") {
        val assistantUser = AppUser(
            uid = "3",
            email = "assistant@test.com",
            displayName = "Assistant",
            role = UserRole.SPEAKING_ASSISTANT
        )

        Then("canEditSpeeches should return false") {
            assistantUser.canEditSpeeches().shouldBeFalse()
        }

        Then("canManageUsers should return false") {
            assistantUser.canManageUsers().shouldBeFalse()
        }

        Then("canViewInternalNotes should return false") {
            assistantUser.canViewInternalNotes().shouldBeFalse()
        }
    }

    Given("An AppUser with role NONE (or any other potential role)") {
        // Assuming NONE or USER exists, adjust based on your UserRole enum
        val normalUser = AppUser(
            uid = "4",
            email = "user@test.com",
            displayName = "User",
            role = UserRole.NONE
        )

        Then("canEditSpeeches should return false") {
            normalUser.canEditSpeeches().shouldBeFalse()
        }

        Then("canManageUsers should return false") {
            normalUser.canManageUsers().shouldBeFalse()
        }

        Then("canViewInternalNotes should return true") {
            // Based on logic: role != SPEAKING_ASSISTANT
            normalUser.canViewInternalNotes().shouldBeTrue()
        }
    }
})
