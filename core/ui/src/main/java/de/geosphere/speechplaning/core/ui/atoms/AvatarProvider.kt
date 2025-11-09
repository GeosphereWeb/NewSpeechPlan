package de.geosphere.speechplaning.core.ui.atoms

import androidx.annotation.DrawableRes
import de.geosphere.speechplaning.core.model.data.SpiritualStatus

/**
 * Stellt die Logik bereit, um den passenden Avatar für einen spirituellen Status zu finden.
 *
 * Diese Klasse ist von den konkreten Drawable-Ressourcen entkoppelt. Sie delegiert die
 * Aufgabe, die tatsächliche Ressourcen-ID zu laden, an eine [AvatarResourceProvider]-Implementierung.
 *
 * @param resourceProvider Eine Implementierung, die die Drawable-IDs bereitstellt.
 */
class AvatarProvider(private val resourceProvider: AvatarResourceProvider) {

    /**
     * Ruft die passende Avatar-Drawable-Ressource über den Provider ab.
     *
     * @param spiritualStatus Der spirituelle Status der Person.
     * @return Eine Integer-ID, die auf die Drawable-Ressource verweist.
     */
    @DrawableRes
    fun getAvatar(spiritualStatus: SpiritualStatus): Int {
        // Die Logik ist jetzt trivial, sie gibt die Anfrage nur weiter.
        // Die "when"-Logik ist in die Implementierung des Interfaces gewandert.
        return resourceProvider.getAvatarResource(spiritualStatus)
    }
}
