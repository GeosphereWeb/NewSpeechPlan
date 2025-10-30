package de.geosphere.speechplaning.resterampe

import android.content.Context
import android.content.res.Resources
import de.geosphere.speechplaning.R
import de.geosphere.speechplaning.core.model.data.Event

/**
 * Maps the [Event] enum to and from its localized string representation.
 * @param context The application context to access string resources.
 */
@Suppress("SwallowedException")
class EventMapper(private val context: Context) {

    // This map is built once and handles all fallbacks correctly.
    private val statusToStringMap: Map<Event, String> by lazy {
        // First, try to get the fallback string for UNKNOWN.
        // If it's not found, the ultimate fallback is an empty string.
        val unknownString = try {
            context.getString(R.string.event_unknown)
        } catch (e: Resources.NotFoundException) {
            ""
        }

        // Create the map for all event types.
        Event.entries.associateWith { status ->
            // Determine the resource ID for the current status.
            val resourceId = when (status) {
                Event.CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER -> R.string.event_circuit_assembly_with_circuit_overseer
                Event.CIRCUIT_OVERSEER_CONGREGATION_VISIT -> R.string.event_circuit_overseer_congregation_visit
                Event.CONVENTION -> R.string.event_convention
                Event.MEMORIAL -> R.string.event_memorial
                Event.SPECIAL_LECTURE -> R.string.event_special_lecture
                Event.MISCELLANEOUS -> R.string.event_miscellaneous
                Event.UNKNOWN -> R.string.event_unknown
            }

            try {
                // Try to get the string for the specific resource.
                context.getString(resourceId)
            } catch (e: Resources.NotFoundException) {
                // If it fails, use the UNKNOWN string we already fetched.
                // This correctly handles the case where UNKNOWN itself might be missing.
                unknownString
            }
        }
    }

    // This map is used for the reverse mapping.
    private val stringToStatusMap: Map<String, Event> by lazy {
        // We need to be careful here. If multiple enums fall back to the same string (e.g., ""),
        // the last one in the association wins. This is acceptable behavior.
        statusToStringMap.entries.associate { (key, value) -> value to key }
    }

    /**
     * Maps a [Event] enum to its display name string.
     * @param status The enum to map.
     * @return The localized string representation.
     */
    fun mapToString(status: Event): String {
        // The map is guaranteed to contain all enum values, so the fallback should not be hit,
        // but it's good practice. The real fallback logic is inside the map's initialization.
        return statusToStringMap[status] ?: ""
    }

    /**
     * Maps a display name string back to a [Event] enum.
     * @param statusString The localized string.
     * @return The corresponding [Event] enum, or [Event.UNKNOWN] if no match is found.
     */
    fun mapToStatus(statusString: String): Event {
        // If the string is not found, default to UNKNOWN.
        return stringToStatusMap[statusString] ?: Event.UNKNOWN
    }
}
