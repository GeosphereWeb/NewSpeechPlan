package de.geosphere.speechplaning.feature.congregationEvent

import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.Congregation

sealed interface CongregationEventUiState {
    data object LoadingUiState : CongregationEventUiState
    data class ErrorUiState(val message: String) : CongregationEventUiState
    data class SuccessUiState(
        val congregationEvents: List<CongregationEvent> = emptyList(),
        val allSpeakers: List<Speaker> = emptyList(),
        val allCongregations: List<Congregation> = emptyList(),
        val allSpeeches: List<Speech> = emptyList(),
        val selectedCongregationEvent: CongregationEvent? = null,
        val showEditDialog: Boolean = false,
        val isActionInProgress: Boolean = false,
        val actionError: String? = null,
        val canCreateCongregationEvent: Boolean = false,
        val canEditCongregationEvent: Boolean = false,
        val canDeleteCongregationEvent: Boolean = false
    ) : CongregationEventUiState
}
