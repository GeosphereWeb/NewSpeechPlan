package de.geosphere.speechplaning.core.ui.atoms

import de.geosphere.speechplaning.core.model.data.SpiritualStatus

interface SpiritualStatusStringProvider {
    fun getStringForSpiritualStatus(spiritualStatus: SpiritualStatus): String
    fun getUnknownSpiritualStatusString(): String
}
