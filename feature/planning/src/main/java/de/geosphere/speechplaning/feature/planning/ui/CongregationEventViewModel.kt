package de.geosphere.speechplaning.feature.planning.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.usecases.planning.DeleteCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.planning.GetCongregationEventsUseCase
import de.geosphere.speechplaning.data.usecases.planning.SaveCongregationEventUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CongregationEventUiState(
    val isLoading: Boolean = false,
    val events: List<CongregationEvent> = emptyList(),
    val error: String? = null,
    val selectedEvent: CongregationEvent? = null
)

class CongregationEventViewModel(
    private val getCongregationEventsUseCase: GetCongregationEventsUseCase,
    private val saveCongregationEventUseCase: SaveCongregationEventUseCase,
    private val deleteCongregationEventUseCase: DeleteCongregationEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CongregationEventUiState())
    val uiState: StateFlow<CongregationEventUiState> = _uiState.asStateFlow()

    fun loadEvents(districtId: String, congregationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            getCongregationEventsUseCase(districtId, congregationId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, events = it)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.localizedMessage)
                }
        }
    }

    fun selectEvent(event: CongregationEvent) {
        _uiState.value = _uiState.value.copy(selectedEvent = event)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedEvent = null)
    }

    fun saveEvent(districtId: String, congregationId: String, event: CongregationEvent) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            saveCongregationEventUseCase(districtId, congregationId, event)
                .onSuccess {
                    loadEvents(districtId, congregationId) // Reload the list
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.localizedMessage)
                }
        }
    }

    fun deleteEvent(districtId: String, congregationId: String, eventId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            deleteCongregationEventUseCase(districtId, congregationId, eventId)
                .onSuccess {
                    loadEvents(districtId, congregationId) // Reload the list
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.localizedMessage)
                }
        }
    }
}
