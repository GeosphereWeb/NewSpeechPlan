package de.geosphere.speechplaning.core.ui.provider

import android.content.Context
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.theme.R

class AppEventStringProvider(private val context: Context) : EventStringProvider {

    override fun getStringForEvent(event: Event): String {
        val resId = when (event) {
            Event.CONGREGATION -> R.string.event_congregation
            Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT -> R.string.event_circuit_overseer_congregation_visit
            Event.CIRCUIT_ASSEMBLY -> R.string.event_circuit_assembly
            Event.CONVENTION -> R.string.event_convention
            Event.MEMORIAL -> R.string.event_memorial
            Event.SPECIAL_LECTURE -> R.string.event_special_lecture
            Event.MISCELLANEOUS -> R.string.event_miscellaneous
            Event.BRANCH_CONVENTION -> R.string.event_branch_convention
            Event.STREAM -> R.string.event_stream
            Event.UNKNOWN -> R.string.event_unknown
        }
        return context.getString(resId)
    }

    override fun getUnknownEventString(): String {
        return context.getString(R.string.event_unknown)
    }
}
