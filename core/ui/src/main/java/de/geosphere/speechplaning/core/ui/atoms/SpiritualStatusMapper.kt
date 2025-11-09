package de.geosphere.speechplaning.core.ui.atoms

import de.geosphere.speechplaning.core.model.data.SpiritualStatus

/**
 * Maps the [de.geosphere.speechplaning.core.model.data.SpiritualStatus] enum to and from its localized string
 * representation.
 *
 * This utility class is responsible for converting between the `SpiritualStatus` enum, which is used
 * in the data layer, and the human-readable, localized strings displayed in the user interface.
 * It ensures that the display strings are correctly retrieved from Android string resources,
 * facilitating internationalization.
 *
 * It uses a lazy-initialized map for efficient lookups.
 *
 * @param context The application context required to access string resources.
 */
class SpiritualStatusMapper(private val spiritualStatusProvider: SpiritualStatusStringProvider) {
    private val stringToStatusMap: Map<String, SpiritualStatus> by lazy {
        SpiritualStatus.entries.associateBy { spiritualStatusProvider.getStringForSpiritualStatus(it) }
    }

    /**
     * Maps a [SpiritualStatus] enum to its display name string.
     *
     * @param status The [SpiritualStatus] enum to be converted.
     * @return The localized string representation of the given status.
     * If the status is not found in the map, it returns the string for [SpiritualStatus.UNKNOWN].
     */
    fun mapToString(spiritualStatus: SpiritualStatus): String {
        return spiritualStatusProvider.getStringForSpiritualStatus(spiritualStatus)
    }

    /**
     * Maps a display name string back to a [SpiritualStatus] enum.
     *
     * This function takes a localized string representation of a spiritual status and
     * attempts to find the corresponding [SpiritualStatus] enum constant. If the string
     * does not match any known status, it defaults to [SpiritualStatus.UNKNOWN].
     *
     * @param statusString The localized string representing the spiritual status.
     * @return The corresponding [SpiritualStatus] enum, or [SpiritualStatus.UNKNOWN] if no match is found.
     */
    fun mapToStatus(spiritualStatusString: String): SpiritualStatus {
        // If the string is not found, default to UNKNOWN.
        return stringToStatusMap[spiritualStatusString] ?: SpiritualStatus.UNKNOWN
    }
}
