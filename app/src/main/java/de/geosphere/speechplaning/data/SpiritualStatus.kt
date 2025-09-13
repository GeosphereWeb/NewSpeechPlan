package de.geosphere.speechplaning.data

import kotlinx.serialization.Serializable

/**
 * Represents the spiritual status of an individual within a congregation.
 *
 * This enum defines the different levels of responsibility and authority that a
 * member of a congregation might hold. Each status is associated with a
 * localized string resource ID that can be used to display the status in the user interface.
 *
 */
@Serializable
enum class SpiritualStatus {
    UNKNOWN,
    MINISTERIAL_SERVANT,
    ELDER,
}
