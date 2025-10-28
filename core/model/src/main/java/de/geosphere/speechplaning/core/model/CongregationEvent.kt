package de.geosphere.speechplaning.core.model

import com.google.firebase.firestore.DocumentId
import de.geosphere.speechplaning.core.model.data.Event
import java.time.LocalDate // Using java.time.LocalDate as discussed

/**
 * Represents a specific event within a congregation's schedule.
 *
 * @property id The unique ID of the event.
 * @property congregationId The ID of the congregation this event belongs to.
 * @property date The date of the event.
 * @property eventType The type of the event (e.g., Public Talk, Watchtower Study).
 * @property speechId The ID of the speech given at this event (nullable).
 * @property speakerId The ID of the speaker for this event (nullable).
 * @property chairmanId The ID of the chairman for this event (optional).
 * @property notes Additional notes for the event.
 */
data class CongregationEvent(
    @DocumentId val id: String = "",
    val congregationId: String = "",
    val date: LocalDate,
    val eventType: Event,
    val speechId: String? = null,
    val speakerId: String? = null,
    val chairmanId: String? = null,
    val notes: String? = null
) : SavableDataClass() // Assuming SavableDataClass is a suitable base class
