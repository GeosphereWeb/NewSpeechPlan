package de.geosphere.speechplaning.feature.speaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.data.usecases.speaker.DeleteSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speaker.SaveSpeakerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SpeakerUiState(
    val isLoading: Boolean = false,
    val speakers: List<Speaker> = emptyList(),
    val error: String? = null
)

class SpeakerViewModel(
    private val getSpeakersUseCase: GetSpeakersUseCase,
    private val saveSpeakerUseCase: SaveSpeakerUseCase,
    private val deleteSpeakerUseCase: DeleteSpeakerUseCase,
    private val districtId: String, // These would typically be provided by a user session or similar
    private val congregationId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpeakerUiState())
    val uiState: StateFlow<SpeakerUiState> = _uiState.asStateFlow()

    init {
        loadSpeakers()
    }

    fun loadSpeakers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            getSpeakersUseCase(districtId, congregationId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, speakers = it)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                }
        }
    }

    fun saveSpeaker(speaker: Speaker) {
        viewModelScope.launch {
            saveSpeakerUseCase(districtId, congregationId, speaker)
                .onSuccess { loadSpeakers() } // Reload the list on success
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun deleteSpeaker(speakerId: String) {
        viewModelScope.launch {
            deleteSpeakerUseCase(districtId, congregationId, speakerId)
                .onSuccess { loadSpeakers() } // Reload the list on success
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }
}
