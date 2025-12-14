package de.geosphere.speechplaning.feature.congregation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.data.authentication.permission.CongregationPermissionPolicy
import de.geosphere.speechplaning.data.usecases.congregation.DeleteCongregationUseCase
import de.geosphere.speechplaning.data.usecases.congregation.GetAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.congregation.SaveCongregationUseCase
import de.geosphere.speechplaning.data.usecases.districts.GetAllDistrictsUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CongregationUiState {
    // Zustand 1: Initiales Laden der Liste
    data object LoadingUIState : CongregationUiState

    // Zustand 2: Fehler beim Laden
    data class ErrorUIState(val message: String) : CongregationUiState

    // Zustand 3: Daten erfolgreich geladen
    // Hier packen wir alles rein, was wir sehen, wenn die Liste da ist.
    // 'isActionInProgress' nutzen wir, um z.B. beim Speichern einen Ladebalken
    // ÜBER der Liste anzuzeigen, ohne die Liste verschwinden zu lassen.
    data class SuccessUIState(
        val congregations: List<Congregation> = emptyList(),
        val selectedCongregation: Congregation? = null,
        val isActionInProgress: Boolean = false,
        val allDistricts: List<District> = emptyList(),
        val actionError: String? = null,

        // --- HIER KOMMEN DIE NEUEN FELDER HIN ---
        // Statt nur 'canEdit', splitten wir das auf:
        val canCreateCongregation: Boolean = false, // Darf neue anlegen
        val canEditCongregation: Boolean = false, // Darf existierende ändern
        val canDeleteCongregation: Boolean = false, // Darf löschen (nur Admin)
    ) : CongregationUiState
}

class CongregationViewModel(
    private val saveCongregationUseCase: SaveCongregationUseCase,
    private val deleteCongregationUseCase: DeleteCongregationUseCase,
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val getAllDistrictsUseCase: GetAllDistrictsUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: CongregationPermissionPolicy
) : ViewModel() {

    // Lokaler State für UI-Dinge, die nicht in der DB stehen (Dialoge, Ladeanzeigen bei Aktionen)
    private val _viewState = MutableStateFlow(CongregationViewState())

    // Wenn wir speichern, brauchen wir aber zwingend noch einen District.
    // Wenn die Congregation neu ist, setzen wir hier deinen Default-District.
    private val defaultDistrictId = "777"

    /**
     * Der UI-Status ist eine Kombination aus drei Datenströmen:
     * 1. Die Liste der Versammlungen aus der Datenbank (GetCongregationUseCase)
     * 2. Der aktuelle User und seine Rechte (ObserveCurrentUserUseCase)
     * 3. Der lokale View-Status (Selektion, Fehlertexte, Lade-Spinner)
     * 4. Die Liste aller Districts (GetAllDistrictsUseCase)
     */
    val uiState: StateFlow<CongregationUiState> = combine(
        getAllCongregationsUseCase(),
        observeCurrentUserUseCase(),
        getAllDistrictsUseCase(),
        _viewState
    ) { allCongregations, appUser, allDistrictsResult, viewState ->
        allCongregations.onFailure { exception ->
            android.util.Log.e("CongregationViewModel", "Fehler beim Laden aller Versammlungen", exception)
        }
        val congregationsList = allCongregations.getOrElse { emptyList() }
        val districtsList = allDistrictsResult.getOrElse { emptyList() }

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
        CongregationUiState.SuccessUIState(
            congregations = congregationsList,
            selectedCongregation = viewState.selectedCongregation,
            isActionInProgress = viewState.isActionInProgress,
            actionError = viewState.actionError,
            allDistricts = districtsList,
            // Zuweisung der berechneten Werte an den State
            canCreateCongregation = canCreate,
            canEditCongregation = canEdit,
            canDeleteCongregation = canDelete
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CongregationUiState.LoadingUIState
    )

    // --- User Aktionen ---

    /**
     * Wird aufgerufen, wenn eine Versammlung angeklickt wird (zum Bearbeiten)
     */
    fun selectCongregation(congregation: Congregation) {
        _viewState.value = _viewState.value.copy(selectedCongregation = congregation)
    }

    /**
     * Schließt den Dialog / hebt die Auswahl auf
     */
    fun clearSelection() {
        _viewState.value = _viewState.value.copy(selectedCongregation = null)
    }

    /**
     * Speichert eine Versammlung. Prüft vorher zur Sicherheit noch einmal die Admin-Rechte.
     */
    fun saveCongregation(congregation: Congregation) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            // 1. SICHERHEITSCHECK MIT POLICY
            // Wir unterscheiden: Ist es eine neue Versammlung (ID leer) oder ein Update?
            val isNew = congregation.id.isBlank()

            val hasPermission = if (currentUser != null) {
                if (isNew) {
                    permissionPolicy.canCreate(currentUser)
                } else {
                    permissionPolicy.canEdit(currentUser, congregation)
                }
            } else {
                false
            }

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

            // ... (Rest wie gehabt: Loading setzen, saveCongregationUseCase aufrufen) ...
            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)
            // Beim SPEICHERN muss die Congregation aber wissen, wohin sie gehört.
            // Entweder hat das Congregation-Objekt selbst eine districtId gespeichert,
            // oder wir nutzen (wie in deinem Code vorher) den Hardcode/Default District.
            val targetDistrict = congregation.districtId.ifBlank { defaultDistrictId }

            saveCongregationUseCase(targetDistrict, congregation)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isActionInProgress = false, selectedCongregation = null)
                }
                .onFailure { error ->
                    _viewState.value =
                        _viewState.value.copy(isActionInProgress = false, actionError = error.localizedMessage)
                }
        }
    }

    /**
     * Löscht eine Versammlung nach strenger Prüfung.
     */
    fun deleteCongregation(congregationId: String) {
        viewModelScope.launch {
            // 1. Aktuellen User laden
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            // 2. Die zu löschende Versammlung aus dem aktuellen UI-State holen
            // Da wir reaktiv sind, haben wir die Liste meistens schon im Speicher.
            // Wir suchen die Versammlung in der aktuellen Liste.
            val congregationToDelete = (uiState.value as? CongregationUiState.SuccessUIState)
                ?.congregations
                ?.find { it.id == congregationId }

            // Falls die Versammlung im State nicht gefunden wurde (z.B. durch Race Condition),
            // brechen wir sicherheitshalber ab oder laden sie notfalls nach.
            if (congregationToDelete == null) {
                _viewState.value = _viewState.value.copy(actionError = "Versammlung nicht gefunden.")
                return@launch
            }

            // 3. Strenge Prüfung mit der Policy und dem ECHTEN Objekt
            val hasPermission = currentUser != null && permissionPolicy.canDelete(
                currentUser,
                congregationToDelete
            )

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(
                    actionError = "Keine Berechtigung zum Löschen dieser Versammlung!"
                )
                return@launch
            }

            // 4. Loading setzen
            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            // 5. Löschen ausführen
            deleteCongregationUseCase(congregationToDelete.id, congregationToDelete.districtId)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(
                        isActionInProgress = false,
                        selectedCongregation = null
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
private data class CongregationViewState(
    val selectedCongregation: Congregation? = null,
    val isActionInProgress: Boolean = false,
    val actionError: String? = null
)
