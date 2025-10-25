package de.geosphere.speechplaning.core.model

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.Serializable


@Serializable
data class District(
    @DocumentId val id: String = "",
    val circuitOverseerId: String = "",
    val name: String = "",
    val active: Boolean = true,
) : SavableDataClass()
