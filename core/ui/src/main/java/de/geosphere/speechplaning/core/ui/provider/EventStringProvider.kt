package de.geosphere.speechplaning.core.ui.provider

import de.geosphere.speechplaning.core.model.data.Event

interface EventStringProvider {
    fun getStringForEvent(event: Event): String
    fun getUnknownEventString(): String
}
