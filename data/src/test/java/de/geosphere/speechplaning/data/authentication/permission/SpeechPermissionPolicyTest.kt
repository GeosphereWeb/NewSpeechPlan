package de.geosphere.speechplaning.data.authentication.permission

import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.UserRole
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class SpeechPermissionPolicyTest : BehaviorSpec({

    val policy = SpeechPermissionPolicy()
    val dummySpeech = Speech(id = "1", number = "1", subject = "Test")

    Given("canCreate check") {
        When("User is ADMIN") {
            val user = AppUser(uid = "1", role = UserRole.ADMIN, email = "", displayName = "")
            Then("should return true") {
                policy.canCreate(user).shouldBeTrue()
            }
        }
        When("User is SPEAKING_PLANER") {
            val user =
                AppUser(uid = "2", role = UserRole.SPEAKING_PLANER, email = "", displayName = "")
            Then("should return true") {
                policy.canCreate(user).shouldBeTrue()
            }
        }
        When("User is SPEAKING_ASSISTANT") {
            val user =
                AppUser(uid = "3", role = UserRole.SPEAKING_ASSISTANT, email = "", displayName = "")
            Then("should return false") {
                policy.canCreate(user).shouldBeFalse()
            }
        }
        When("User is NONE") {
            val user = AppUser(uid = "4", role = UserRole.NONE, email = "", displayName = "")
            Then("should return false") {
                policy.canCreate(user).shouldBeFalse()
            }
        }
    }

    Given("canEdit check") {
        When("User is ADMIN") {
            val user = AppUser(uid = "1", role = UserRole.ADMIN, email = "", displayName = "")
            Then("should return true") {
                policy.canEdit(user, dummySpeech).shouldBeTrue()
            }
        }
        When("User is SPEAKING_PLANER") {
            val user =
                AppUser(uid = "2", role = UserRole.SPEAKING_PLANER, email = "", displayName = "")
            Then("should return true") {
                policy.canEdit(user, dummySpeech).shouldBeTrue()
            }
        }
        When("User is other role") {
            val user =
                AppUser(uid = "3", role = UserRole.SPEAKING_ASSISTANT, email = "", displayName = "")
            Then("should return false") {
                policy.canEdit(user, dummySpeech).shouldBeFalse()
            }
        }
    }

    Given("canDelete check") {
        When("User is ADMIN") {
            val user = AppUser(uid = "1", role = UserRole.ADMIN, email = "", displayName = "")
            Then("should return true") {
                policy.canDelete(user, dummySpeech).shouldBeTrue()
            }
        }
        When("User is SPEAKING_PLANER") {
            val user =
                AppUser(uid = "2", role = UserRole.SPEAKING_PLANER, email = "", displayName = "")
            Then("should return true") {
                policy.canDelete(user, dummySpeech).shouldBeTrue()
            }
        }
        When("User is other role") {
            val user =
                AppUser(uid = "3", role = UserRole.SPEAKING_ASSISTANT, email = "", displayName = "")
            Then("should return false") {
                policy.canDelete(user, dummySpeech).shouldBeFalse()
            }
        }
    }

    Given("canManageGeneral check") {
        When("User is ADMIN") {
            val user = AppUser(uid = "1", role = UserRole.ADMIN, email = "", displayName = "")
            Then("should return true") {
                policy.canManageGeneral(user).shouldBeTrue()
            }
        }
        When("User is SPEAKING_PLANER") {
            val user =
                AppUser(uid = "2", role = UserRole.SPEAKING_PLANER, email = "", displayName = "")
            Then("should return true") {
                policy.canManageGeneral(user).shouldBeTrue()
            }
        }
        When("User is other role") {
            val user =
                AppUser(uid = "3", role = UserRole.SPEAKING_ASSISTANT, email = "", displayName = "")
            Then("should return false") {
                policy.canManageGeneral(user).shouldBeFalse()
            }
        }
    }
})
