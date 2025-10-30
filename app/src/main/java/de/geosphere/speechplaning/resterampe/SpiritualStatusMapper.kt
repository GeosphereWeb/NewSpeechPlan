package de.geosphere.speechplaning.resterampe

import android.content.Context
import de.geosphere.speechplaning.R
import de.geosphere.speechplaning.core.model.data.SpiritualStatus

/**
 * Maps the [SpiritualStatus] enum to and from its localized string representation.
 * @param context The application context to access string resources.
 */
class SpiritualStatusMapper(private val context: Context) {

    private val statusToStringMap by lazy {
        SpiritualStatus.entries.associateWith { status ->
            val resourceId = when (status) {
                SpiritualStatus.ELDER -> R.string.spiritual_status_elder
                SpiritualStatus.MINISTERIAL_SERVANT -> R.string.spiritual_status_ministerial_servant
                SpiritualStatus.UNKNOWN -> R.string.spiritual_status_unknown
            }
            context.getString(resourceId)
        }
    }

    private val stringToStatusMap by lazy {
        statusToStringMap.entries.associate { (k, v) -> v to k }
    }

    /**
     * Maps a [SpiritualStatus] enum to its display name string.
     * @param status The enum to map.
     * @return The localized string representation.
     */
    fun mapToString(status: SpiritualStatus): String {
        return statusToStringMap[status] ?: statusToStringMap[SpiritualStatus.UNKNOWN].orEmpty()
    }

    /**
     * Maps a display name string back to a [SpiritualStatus] enum.
     * @param statusString The localized string.
     * @return The corresponding [SpiritualStatus] enum, or [SpiritualStatus.UNKNOWN] if no match is found.
     */
    fun mapToStatus(statusString: String): SpiritualStatus {
        return stringToStatusMap[statusString] ?: SpiritualStatus.UNKNOWN
    }
}
