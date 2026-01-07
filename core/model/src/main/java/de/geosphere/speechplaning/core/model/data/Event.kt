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
    CONGREGATION, //Versammlung
    CIRCUIT_OVERSEER_CONGREGATION_VISIT, // Dienstwoche,
    CIRCUIT_ASSEMBLY,// Kreiskongress
    CONVENTION, // Regionaler Kongress,
    MEMORIAL, // Gedächnismal
    SPECIAL_LECTURE, // Sondervortrag
    MISCELLANEOUS, // Sonstiges
    BRANCH_CONVENTION,  // Zweigbesuch
    STREAM, // Stream
    UNKNOWN,
}
