package de.geosphere.speechplaning.provider

import android.content.Context
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.atoms.EventStringProvider
import de.geosphere.speechplaning.theme.R

class AppEventStringProvider(private val context: Context) : EventStringProvider {

    override fun getStringForEvent(event: Event): String {
        val resId = when (event) {
            Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER -> R.string.event_circuit_assembly_with_circuit_overseer
            Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT -> R.string.event_circuit_overseer_congregation_visit
            Event.CONVENTION -> R.string.event_convention
            Event.MEMORIAL -> R.string.event_memorial
            Event.SPECIAL_LECTURE -> R.string.event_special_lecture
            Event.MISCELLANEOUS -> R.string.event_miscellaneous
            Event.UNKNOWN -> R.string.event_unknown
        }
        return context.getString(resId)
    }

    override fun getUnknownEventString(): String {
        return context.getString(R.string.event_unknown)
    }
}
