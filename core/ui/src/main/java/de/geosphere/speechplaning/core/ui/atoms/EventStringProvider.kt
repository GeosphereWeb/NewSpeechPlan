package de.geosphere.speechplaning.core.ui.atoms

import de.geosphere.speechplaning.core.model.data.Event

interface EventStringProvider {
    fun getStringForEvent(event: Event): String
    fun getUnknownEventString(): String
}
