package de.geosphere.speechplaning.core.ui.provider

import de.geosphere.speechplaning.core.model.data.Event

interface EventIconProvider {
    fun getIconForEvent(event: Event): Int
    fun getUnknownEventIcon(): String
}
