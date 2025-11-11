package de.geosphere.speechplaning.core.model.data

import kotlinx.serialization.Serializable

/**
 * Enum class representing different types of events.
 *
 * Each event has a resource ID associated with it, which can be used to retrieve
 * the string representation of the event's name in a given context.
 */
@Serializable
enum class Event {
    CIRCUIT_ASSEMBLY_WITH_CIRCUIT_OVERSEER,
    CIRCUIT_OVERSEER_CONGREGATION_VISIT,
    CONVENTION,
    MEMORIAL,
    SPECIAL_LECTURE,
    MISCELLANEOUS,
    UNKNOWN,
}
