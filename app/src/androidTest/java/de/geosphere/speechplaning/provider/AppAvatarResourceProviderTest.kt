package de.geosphere.speechplaning.provider

import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.theme.R
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

// WICHTIG: Dieser Test muss im `androidTest` Verzeichnis liegen,
// da er auf Android-Ressourcen (R-Klasse) zugreift.
class AppAvatarResourceProviderTest : StringSpec({

    val provider = AppAvatarResourceProvider()

    "getAvatarResource for ELDER should return the correct drawable" {
        val expectedDrawableId = R.drawable.business_man_man_avatar_icon
        val actualDrawableId = provider.getAvatarResource(SpiritualStatus.ELDER)
        actualDrawableId shouldBe expectedDrawableId
    }

    "getAvatarResource for MINISTERIAL_SERVANT should return the correct drawable" {
        val expectedDrawableId = R.drawable.man_avatar_male_icon
        val actualDrawableId = provider.getAvatarResource(SpiritualStatus.MINISTERIAL_SERVANT)
        actualDrawableId shouldBe expectedDrawableId
    }

    "getAvatarResource for UNKNOWN should return the correct drawable" {
        val expectedDrawableId = R.drawable.man_goatee_user_avatar_icon
        val actualDrawableId = provider.getAvatarResource(SpiritualStatus.UNKNOWN)
        actualDrawableId shouldBe expectedDrawableId
    }
})
