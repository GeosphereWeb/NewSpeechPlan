package de.geosphere.speechplaning.ui.atoms

import de.geosphere.speechplaning.R
import de.geosphere.speechplaning.data.SpiritualStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class AvatarProviderTest {

    @ParameterizedTest
    @EnumSource(SpiritualStatus::class)
    fun `getAvatar returns correct drawable for each spiritual status`(status: SpiritualStatus) {
        val expectedDrawable = when (status) {
            SpiritualStatus.ELDER -> R.drawable.business_man_man_avatar_icon
            SpiritualStatus.MINISTERIAL_SERVANT -> R.drawable.man_avatar_male_icon
            SpiritualStatus.UNKNOWN -> R.drawable.man_goatee_user_avatar_icon
        }
        assertEquals(expectedDrawable, AvatarProvider.getAvatar(status))
    }
}
