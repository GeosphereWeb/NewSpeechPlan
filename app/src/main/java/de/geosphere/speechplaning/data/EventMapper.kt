package de.geosphere.speechplaning.data

import android.content.Context
import android.content.res.Resources
import de.geosphere.speechplaning.R

/**
 * Maps the [Event] enum to and from its localized string representation.
 * @param context The application context to access string resources.
 */
class EventMapper(private val context: Context) {

    private val statusToStringMap: Map<Event, String> by lazy {
        val unknownString = try {
            context.getString(R.string.event_unknown)
        } catch (e: Resources.NotFoundException) {
            "" // Final fallback if UNKNOWN itself is not found
        }

        Event.entries.associateWith { status ->
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
                context.getString(resourceId)
            } catch (e: Resources.NotFoundException) {
                unknownString // Fallback for any missing string
            }
        }
    }

    private val stringToStatusMap by lazy {
        statusToStringMap.entries.associate { (k, v) -> v to k }
    }


    /**
     * Maps a [Event] enum to its display name string.
     * @param status The enum to map.
     * @return The localized string representation.
     */
    fun mapToString(status: Event): String {
        return statusToStringMap[status] ?: ""
    }

    /**
     * Maps a display name string back to a [Event] enum.
     * @param statusString The localized string.
     * @return The corresponding [Event] enum, or [Event.UNKNOWN] if no match is found.
     */
    fun mapToStatus(statusString: String): Event {
        return stringToStatusMap[statusString] ?: Event.UNKNOWN
    }
}
