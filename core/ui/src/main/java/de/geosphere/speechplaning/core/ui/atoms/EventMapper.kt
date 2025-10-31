package de.geosphere.speechplaning.core.ui.atoms

import de.geosphere.speechplaning.core.model.data.Event

/**
 * Maps an [Event] to its localized string representation and vice versa.
 *
 * This class provides a centralized way to handle the conversion between the `Event` enum
 * and the user-facing strings defined in the application's resources. It uses an
 * [EventStringProvider] to fetch the appropriate localized strings.
 *
 * It is primarily used to display event types in the UI and to parse user input or stored
 * string values back into the corresponding enum.
 *
 * @param stringProvider An implementation that provides the localized string for a given [Event].
 */
@Suppress("SwallowedException")
class EventMapper(private val stringProvider: EventStringProvider) {
    private val stringToStatusMap: Map<String, Event> by lazy {
        Event.entries.associateBy { stringProvider.getStringForEvent(it) }
    }

    /**
     * Maps a [Event] enum to its display name string.
     * @param status The enum to map.
     * @return The localized string representation.
     */
    fun mapToString(status: Event): String {
        return stringProvider.getStringForEvent(status)
    }

    /**
     * Maps a display name string back to a [Event] enum.
     *
     * @param statusString The localized string representation of an event status.
     * @return The corresponding [Event] enum. If the string does not match any known event status,
     *         it defaults to [Event.UNKNOWN].
     */
    fun mapToStatus(statusString: String): Event {
        // If the string is not found, default to UNKNOWN.
        return stringToStatusMap[statusString] ?: Event.UNKNOWN
    }
}
