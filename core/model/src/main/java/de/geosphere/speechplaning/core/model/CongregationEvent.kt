package de.geosphere.speechplaning.core.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import de.geosphere.speechplaning.core.model.data.Event
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class CongregationEvent(
    @DocumentId val id: String = "",
    val dateString: String? = null, // For Firestore
    val eventType: Event = Event.CONVENTION,

    val speechId: String? = null,
    val speechNumber: String? = null,
    val speechSubject: String? = null,

    val speakerId: String? = null,
    val speakerName: String? = null,

    val speakerCongregationId: String? = null,
    val speakerCongregationName: String? = null,

    val notes: String? = null,
) : SavableDataClass() {
    @get:Exclude
    val date: LocalDate?
        get() = dateString?.let { LocalDate.parse(it) }
}
