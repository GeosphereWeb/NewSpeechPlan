package de.geosphere.speechplaning.feature.districts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.authentication.permission.DistrictPermissionPolicy
import de.geosphere.speechplaning.data.usecases.districts.DeleteDistrictUseCase
import de.geosphere.speechplaning.data.usecases.districts.GetDistrictUseCase
import de.geosphere.speechplaning.data.usecases.districts.SaveDistrictUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DistrictUiState {
    data object LoadingUIState : DistrictUiState

    // Zustand 2: Fehler beim Laden
    data class ErrorUIState(val message: String) : DistrictUiState
    data class SuccessUIState(
        val districts: List<District> = emptyList(),
        val selectedDistrict: District? = null,
        val isActionInProgress: Boolean = false,
        val actionError: String? = null,

        // --- HIER KOMMEN DIE NEUEN FELDER HIN ---
        // Statt nur 'canEdit', splitten wir das auf:
        val canCreateSpeech: Boolean = false, // Darf neue anlegen
        val canEditSpeech: Boolean = false, // Darf existierende ändern
        val canDeleteSpeech: Boolean = false // Darf löschen (nur Admin)
    ): DistrictUiState
}

class DistrictsViewModel(
    private val getDistrictUseCase: GetDistrictUseCase,
    private val saveDistrictUseCase: SaveDistrictUseCase,
    private val deleteDistrictUseCase: DeleteDistrictUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: DistrictPermissionPolicy,
    // private val districtRepository: DistrictRepositoryImpl,
) : ViewModel() {

    private val _viewState = MutableStateFlow(DistrictViewState())

    val uiState: StateFlow<DistrictUiState> = combine(
        getDistrictUseCase(),
        observeCurrentUserUseCase,
        _viewState
    ) { speechesResult, appUser, viewState ->
        val speechList = speechesResult.getOrElse { emptyList() }

        // / 1. BERECHTIGUNGEN PRÜFEN MIT POLICY
        var canCreate = false
        var canEdit = false
        var canDelete = false

        if (appUser != null) {
            // Darf er generell erstellen?
            canCreate = permissionPolicy.canCreate(appUser)

            // Für die Liste: Darf er generell bearbeiten/löschen?
            // (Hier nehmen wir an: Wer generell verwalten darf, bekommt die Buttons angezeigt.
            // Die feine Prüfung pro Rede passiert beim Klick oder im Dialog)
            canEdit = permissionPolicy.canManageGeneral(appUser)
            canDelete = permissionPolicy.canManageGeneral(appUser)
        }

        // 3. Alles zum UI State zusammenbauen
        DistrictUiState.SuccessUIState(
            districts = speechList,
            selectedDistrict = viewState.selectedDistrict,
            isActionInProgress = viewState.isActionInProgress,
            actionError = viewState.actionError,
            // Zuweisung der berechneten Werte an den State
            canCreateSpeech = canCreate,
            canEditSpeech = canEdit,
            canDeleteSpeech = canDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DistrictUiState.LoadingUIState
    )

    // init {
    //     loadDistricts()
    // }

    // private fun loadDistricts() {
    //     viewModelScope.launch {
    //         _uiState.value = DistrictsUiState.Loading
    //         getDistrictUseCase()
    //             .catch { exception ->
    //                 _uiState.value = DistrictsUiState.Error(exception.message ?: "An unknown error occurred")
    //             }
    //             .collect { districts ->
    //                 _uiState.value = DistrictsUiState.Success(districts = districts)
    //             }
    //     }
    // }

    fun selectDistrict(district: District) {
        (_uiState.value as? DistrictUiState.Success)?.let { currentState ->
            _uiState.value = currentState.copy(selectedDistrict = district)
        }
    }

    fun clearSelection() {
        (_uiState.value as? DistrictUiState.Success)?.let { currentState ->
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
                deleteDistrictUseCase(districtId)
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

    private fun updateSuccessState(update: (DistrictUiState.Success) -> DistrictUiState.Success) {
        _uiState.update { currentState ->
            if (currentState is DistrictUiState.Success) {
                update(currentState)
            } else {
                currentState
            }
        }
    }
}

/**
 * Interne Hilfsklasse für den lokalen View-Status.
 * Diese Daten kommen nicht aus der DB, sondern entstehen durch UI-Interaktion.
 */
private data class DistrictViewState(
    val selectedDistrict: District? = null,
    val isActionInProgress: Boolean = false,
    val actionError: String? = null
)
