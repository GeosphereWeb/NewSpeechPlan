package de.geosphere.speechplaning.core.model

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

/**
 * Congregation.
 *
 * @property id
 * @property districtId
 * @property name
 * @property address
 * @property meetingTime
 * @property active
 * @constructor Create empty Congregation
 */
@Serializable
data class Congregation(
    @DocumentId val id: String = "",
    val districtId: String = "",
    val name: String = "",
    val address: String = "",
    val meetingTime: String = "",
    val active: Boolean = true,
) : SavableDataClass()
