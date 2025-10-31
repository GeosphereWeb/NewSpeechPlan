package de.geosphere.speechplaning.provider

import android.content.Context
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.core.ui.atoms.SpiritualStatusStringProvider
import de.geosphere.speechplaning.theme.R

class AppSpiritualStatusStringProvider(private val context: Context) : SpiritualStatusStringProvider {

    override fun getStringForSpiritualStatus(spiritualStatus: SpiritualStatus): String {
        val resId = when (spiritualStatus) {
            SpiritualStatus.MINISTERIAL_SERVANT -> R.string.spiritual_status_ministerial_servant
            SpiritualStatus.ELDER -> R.string.spiritual_status_elder
            SpiritualStatus.UNKNOWN -> R.string.spiritual_status_unknown
        }
        return context.getString(resId)
    }

    override fun getUnknownSpiritualStatusString(): String {
        // KORRIGIERT: Hier muss die korrekte Ressource für den Status verwendet werden.
        return context.getString(R.string.spiritual_status_unknown)
    }
}
