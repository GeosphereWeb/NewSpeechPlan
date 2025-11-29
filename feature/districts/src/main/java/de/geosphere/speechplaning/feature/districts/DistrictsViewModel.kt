package de.geosphere.speechplaning.feature.districts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.repository.DistrictRepositoryImpl
import de.geosphere.speechplaning.data.usecases.districts.SaveDistrictUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DistrictsUiState {
    data class Success(
        val districts: List<District> = emptyList(),
        val selectedDistrict: District? = null,
        val isActionInProgress: Boolean = false,
        val actionError: String? = null,
        // Assuming all users can edit districts for now
        val canEditDistrict: Boolean = true
    ) : DistrictsUiState
    data class Error(val message: String) : DistrictsUiState
    data object Loading : DistrictsUiState
}

class DistrictsViewModel(
    private val districtRepository: DistrictRepositoryImpl,
    private val saveDistrictUseCase: SaveDistrictUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DistrictsUiState>(DistrictsUiState.Loading)
    val uiState: StateFlow<DistrictsUiState> = _uiState.asStateFlow()

    init {
        loadDistricts()
    }

    private fun loadDistricts() {
        viewModelScope.launch {
            _uiState.value = DistrictsUiState.Loading
            districtRepository.getAllDistrictFlow()
                .catch { exception ->
                    _uiState.value = DistrictsUiState.Error(exception.message ?: "An unknown error occurred")
                }
                .collect { districts ->
                    _uiState.value = DistrictsUiState.Success(districts = districts)
                }
        }
    }

    fun selectDistrict(district: District) {
        (_uiState.value as? DistrictsUiState.Success)?.let { currentState ->
            _uiState.value = currentState.copy(selectedDistrict = district)
        }
    }

    fun clearSelection() {
        (_uiState.value as? DistrictsUiState.Success)?.let { currentState ->
            _uiState.value = currentState.copy(selectedDistrict = null)
        }
    }

    fun saveDistrict(district: District) {
        viewModelScope.launch {
            updateSuccessState { it.copy(isActionInProgress = true, actionError = null) }
            try {
                saveDistrictUseCase(district)
                updateSuccessState { it.copy(isActionInProgress = false, selectedDistrict = null) }
                // No need to reload, Firestore will push updates. If not using Firestore, call loadDistricts().
            } catch (e: Exception) {
                updateSuccessState {
                    it.copy(
                        isActionInProgress = false,
                        actionError = e.message ?: "Failed to save district"
                    )
                }
            }
        }
    }

    fun deleteDistrict(districtId: String) {
        viewModelScope.launch {
            updateSuccessState { it.copy(isActionInProgress = true, actionError = null) }
            try {
                // Assuming a deleteDistrict function exists in the repository
                districtRepository.deleteDistrict(districtId)
                updateSuccessState { it.copy(isActionInProgress = false, selectedDistrict = null) }
            } catch (e: Exception) {
                updateSuccessState {
                    it.copy(
                        isActionInProgress = false,
                        actionError = e.message ?: "Failed to delete district"
                    )
                }
            }
        }
    }

    private fun updateSuccessState(update: (DistrictsUiState.Success) -> DistrictsUiState.Success) {
        _uiState.update { currentState ->
            if (currentState is DistrictsUiState.Success) {
                update(currentState)
            } else {
                currentState
            }
        }
    }
}
