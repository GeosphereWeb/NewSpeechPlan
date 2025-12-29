@file:Suppress("MatchingDeclarationName")

package de.geosphere.speechplaning.core.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import kotlinx.serialization.Serializable

/**
 * Represents a speaker with their personal information and details.
 *
 * @property id The unique identifier for the speaker. This is typically assigned by Firestore.
 * @property districtId The unique identifier of the district the speaker belongs to.
 * @property firstName The first name of the speaker.
 * @property lastName The last name of the speaker.
 * @property mobile The mobile phone number of the speaker.
 * @property phone The landline phone number of the speaker.
 * @property email The email address of the speaker.
 * @property spiritualStatus The spiritual status of the speaker (e.g., Elder, Ministerial Servant).
 * @property speechNumberIds A list of IDs representing the speech numbers the speaker is qualified to give.
 * @property congregationId The ID of the congregation to which the speaker belongs.
 * @property active Indicates whether the speaker is currently active and available for assignments.
 */
@Serializable
@Keep
data class Speaker(
    @DocumentId val id: String = "",
    val districtId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val mobile: String = "",
    val phone: String = "",
    val email: String = "",
    val spiritualStatus: SpiritualStatus = SpiritualStatus.UNKNOWN,
    val speechNumberIds: List<Int> = emptyList(),
    val congregationId: String = "",
    val active: Boolean = true,
) : SavableDataClass()
