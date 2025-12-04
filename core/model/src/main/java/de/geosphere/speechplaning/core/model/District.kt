package de.geosphere.speechplaning.core.model

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable

@Serializable
data class District(
    @DocumentId val id: String = "",
    val name: String = "",
    val circuitOverseerId: String = "",
    val districtLeaderId: String = "",
    val districtLeaderCongregationId: String = "",
    val active: Boolean = true,
) : SavableDataClass()
