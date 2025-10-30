package de.geosphere.speechplaning.ui.atoms

import de.geosphere.speechplaning.R
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class AvatarProviderTest : FunSpec({

    // Definiert eine Datenklasse, um die Testdaten (Status und erwartetes Drawable) zu kapseln.
    data class AvatarTestData(val status: SpiritualStatus, val expectedDrawable: Int)

    // Ein Context-Block, um zusammengehörige Tests zu gruppieren.
    context("getAvatar returns correct drawable for each spiritual status") {
        // withData führt für jede Zeile einen eigenen Testfall aus. [2, 3]
        // Das ist das Kotest-Äquivalent zum parametrisierten Test.
        withData(
            // Hier werden die Testdaten definiert, ähnlich wie bei @EnumSource.
            nameFn = { "for status ${it.status} it should return the correct drawable" },
            AvatarTestData(SpiritualStatus.ELDER, R.drawable.business_man_man_avatar_icon),
            AvatarTestData(SpiritualStatus.MINISTERIAL_SERVANT, R.drawable.man_avatar_male_icon),
            AvatarTestData(SpiritualStatus.UNKNOWN, R.drawable.man_goatee_user_avatar_icon),
        ) { (status, expectedDrawable) ->
            // Dies ist der eigentliche Testcode, der für jede Datenzeile ausgeführt wird.
            val actualDrawable = AvatarProvider.getAvatar(status)
            actualDrawable shouldBe expectedDrawable
        }
    }
})
