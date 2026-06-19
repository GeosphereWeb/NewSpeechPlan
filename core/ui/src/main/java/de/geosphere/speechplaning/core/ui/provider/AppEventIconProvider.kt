package de.geosphere.speechplaning.core.ui.provider

import android.content.Context
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.theme.R

class AppEventIconProvider(private val context: Context) : EventIconProvider {

    override fun getIconForEvent(event: Event): Int {
        val resId = when (event) {
            Event.CONGREGATION -> -1
            Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT -> R.drawable.persons_doorstep
            Event.CIRCUIT_ASSEMBLY -> R.drawable.arena
            Event.CONVENTION -> R.drawable.arena
            Event.MEMORIAL -> R.drawable.wine_bread
            Event.SPECIAL_LECTURE -> R.drawable.document_speaker
            Event.MISCELLANEOUS -> -1
            Event.BRANCH_CONVENTION -> R.drawable.arena__fill
            Event.STREAM -> -1
            Event.SPECIAL_CONVENTION -> R.drawable.arena
            Event.UNKNOWN -> -1
        }
        return resId
    }

    override fun getUnknownEventIcon(): String {
        return context.getString(R.string.event_unknown)
    }
}
