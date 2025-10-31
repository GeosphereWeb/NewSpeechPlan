package de.geosphere.speechplaning.provider

import androidx.annotation.DrawableRes
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.core.ui.atoms.AvatarResourceProvider
import de.geosphere.speechplaning.theme.R

/**
 * Die konkrete Implementierung von [AvatarResourceProvider] für die App.
 * Diese Klasse kennt die `R`-Klasse aus dem `:theme`-Modul und verbindet
 * den Enum-Status mit den tatsächlichen Drawable-IDs.
 */
class AppAvatarResourceProvider : AvatarResourceProvider {
    @DrawableRes
    override fun getAvatarResource(status: SpiritualStatus): Int {
        return when (status) {
            SpiritualStatus.ELDER -> R.drawable.business_man_man_avatar_icon
            SpiritualStatus.MINISTERIAL_SERVANT -> R.drawable.man_avatar_male_icon
            else -> R.drawable.man_goatee_user_avatar_icon
        }
    }
}
