package de.geosphere.speechplaning.core.ui.atoms

import androidx.annotation.DrawableRes
import de.geosphere.speechplaning.core.model.data.SpiritualStatus

/**
 * Ein Interface, das den Vertrag für die Bereitstellung von Avatar-Drawable-Ressourcen definiert.
 * Module, die Avatare benötigen, hängen von diesem Interface ab, nicht von einer konkreten Implementierung.
 */
interface AvatarResourceProvider {
    /**
     * Liefert die Drawable-Ressourcen-ID für einen gegebenen [SpiritualStatus].
     */
    @DrawableRes
    fun getAvatarResource(status: SpiritualStatus): Int
}
