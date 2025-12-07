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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
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
        val canCreateDistrict: Boolean = false, // Darf neue anlegen
        val canEditDistrict: Boolean = false, // Darf existierende ändern
        val canDeleteDistrict: Boolean = false // Darf löschen (nur Admin)
    ) : DistrictUiState
}

class DistrictsViewModel(
    private val getDistrictUseCase: GetDistrictUseCase,
    private val saveDistrictUseCase: SaveDistrictUseCase,
    private val deleteDistrictUseCase: DeleteDistrictUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: DistrictPermissionPolicy
) : ViewModel() {

    private val _viewState = MutableStateFlow(DistrictViewState())

    val uiState: StateFlow<DistrictUiState> = combine(
        getDistrictUseCase(), // Ruft den Flow im UseCase auf
        observeCurrentUserUseCase(),
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
            canCreateDistrict = canCreate,
            canEditDistrict = canEdit,
            canDeleteDistrict = canDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DistrictUiState.LoadingUIState
    )

    fun selectDistrict(district: District) {
        _viewState.value = _viewState.value.copy(selectedDistrict = district)
    }

    fun clearSelection() {
        _viewState.value = _viewState.value.copy(selectedDistrict = null)
    }

    fun saveDistrict(district: District) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            val isNew = district.id.isBlank()

            val hasPermission = if (currentUser != null) {
                if (isNew) {
                    permissionPolicy.canCreate(currentUser)
                } else {
                    permissionPolicy.canEdit(currentUser, district)
                }
            } else {
                false
            }

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)
            saveDistrictUseCase(district)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isActionInProgress = false, selectedDistrict = null)
                }
                .onFailure { error ->
                    _viewState.value =
                        _viewState.value.copy(isActionInProgress = false, actionError = error.localizedMessage)
                }
        }
    }

    fun deleteDistrict(districtId: String) {
        viewModelScope.launch {
            // 1. Aktuellen User laden
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            // 2. Die zu löschende Rede aus dem aktuellen UI-State holen
            // Da wir reaktiv sind, haben wir die Liste meistens schon im Speicher.
            // Wir suchen die Rede in der aktuellen Liste.
            val districtToDelete = (uiState.value as? DistrictUiState.SuccessUIState)
                ?.districts
                ?.find { it.id == districtId }

            // Falls die Rede im State nicht gefunden wurde (z.B. durch Race Condition),
            // brechen wir sicherheitshalber ab oder laden sie notfalls nach.
            if (districtToDelete == null) {
                _viewState.value = _viewState.value.copy(actionError = "District nicht gefunden.")
                return@launch
            }

            // 3. Strenge Prüfung mit der Policy und dem ECHTEN Speech-Objekt
            val hasPermission = currentUser != null && permissionPolicy.canDelete(currentUser, districtToDelete)

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung zum Löschen dieser Rede!")
                return@launch
            }

            // 4. Loading setzen
            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            // 5. Löschen ausführen
            deleteDistrictUseCase(districtId)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(
                        isActionInProgress = false,
                        selectedDistrict = null
                    )
                }
                .onFailure { error ->
                    _viewState.value = _viewState.value.copy(
                        isActionInProgress = false,
                        actionError = error.localizedMessage ?: "Fehler beim Löschen"
                    )
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
