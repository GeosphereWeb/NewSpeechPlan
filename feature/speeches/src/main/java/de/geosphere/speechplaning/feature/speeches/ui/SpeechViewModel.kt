package de.geosphere.speechplaning.feature.speeches.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.SpeechWithUsageHistory
import de.geosphere.speechplaning.data.authentication.permission.SpeechPermissionPolicy
import de.geosphere.speechplaning.data.usecases.speeches.DeleteSpeechUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesWithUsageCountUseCase
import de.geosphere.speechplaning.data.usecases.speeches.SaveSpeechUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SpeechUiState {
    // Zustand 1: Initiales Laden der Liste
    data object LoadingUIState : SpeechUiState

    // Zustand 2: Fehler beim Laden
    data class ErrorUIState(val message: String) : SpeechUiState

    // Zustand 3: Daten erfolgreich geladen
    // Hier packen wir alles rein, was wir sehen, wenn die Liste da ist.
    // 'isActionInProgress' nutzen wir, um z.B. beim Speichern einen Ladebalken
    // ÜBER der Liste anzuzeigen, ohne die Liste verschwinden zu lassen.
    data class SuccessUIState(
        val speeches: List<SpeechWithUsageHistory> = emptyList(),
        val selectedSpeech: Speech? = null,
        val isActionInProgress: Boolean = false,
        val actionError: String? = null,
        val canCreateSpeech: Boolean = false,
        val canEditSpeech: Boolean = false,
        val canDeleteSpeech: Boolean = false,
        val filterQuery: String = "",
        val showFilterField: Boolean = false,
        val groupByTimesUsed: Boolean = false,
        val filteredSpeeches: List<SpeechWithUsageHistory> = emptyList(),
        val groupedSpeeches: Map<Int, List<SpeechWithUsageHistory>>? = null
    ) : SpeechUiState
}

class SpeechViewModel(
    private val getSpeechesWithUsageCountUseCase: GetSpeechesWithUsageCountUseCase,
    private val saveSpeechUseCase: SaveSpeechUseCase,
    private val deleteSpeechUseCase: DeleteSpeechUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: SpeechPermissionPolicy // <-- NEU: Injiziert
) : ViewModel() {

    // Lokaler State für UI-Dinge, die nicht in der DB stehen (Dialoge, Ladeanzeigen bei Aktionen)
    private val _viewState = MutableStateFlow(SpeechViewState())

    /**
     * Der UI-Status ist eine Kombination aus drei Datenströmen:
     * 1. Die Liste der Reden mit Verwendungszähler (GetSpeechesWithUsageCountUseCase)
     * 2. Der aktuelle User und seine Rechte (ObserveCurrentUserUseCase)
     * 3. Der lokale View-Status (Selektion, Fehlertexte, Lade-Spinner)
     */
    val uiState: StateFlow<SpeechUiState> = combine(
        getSpeechesWithUsageCountUseCase(),
        observeCurrentUserUseCase(),
        _viewState
    ) { speechesWithUsageResult, appUser, viewState ->

        val speechList = speechesWithUsageResult.getOrElse { emptyList() }

        // Berechne gefilterte Liste
        val filteredList = if (viewState.filterQuery.isBlank()) {
            speechList
        } else {
            speechList.filter { speechWithUsage ->
                speechWithUsage.speech.number.contains(viewState.filterQuery, ignoreCase = true) ||
                    speechWithUsage.speech.subject.contains(viewState.filterQuery, ignoreCase = true)
            }
        }

        // Berechne gruppierte Liste nach timesUsed
        val groupedList = if (viewState.groupByTimesUsed) {
            filteredList
                .groupBy { it.timesUsed }
                .toSortedMap(compareBy { it })
        } else {
            null
        }

        // / 1. BERECHTIGUNGEN PRÜFEN MIT POLICY
        var canCreate = false
        var canEdit = false
        var canDelete = false

        if (appUser != null) {
            canCreate = permissionPolicy.canCreate(appUser)
            canEdit = permissionPolicy.canManageGeneral(appUser)
            canDelete = permissionPolicy.canManageGeneral(appUser)
        }

        // 3. Alles zum UI State zusammenbauen
        SpeechUiState.SuccessUIState(
            speeches = speechList,
            selectedSpeech = viewState.selectedSpeech,
            isActionInProgress = viewState.isActionInProgress,
            actionError = viewState.actionError,
            canCreateSpeech = canCreate,
            canEditSpeech = canEdit,
            canDeleteSpeech = canDelete,
            filterQuery = viewState.filterQuery,
            showFilterField = viewState.showFilterField,
            groupByTimesUsed = viewState.groupByTimesUsed,
            filteredSpeeches = filteredList,
            groupedSpeeches = groupedList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpeechUiState.LoadingUIState
    )

    // --- User Aktionen ---

    /**
     * Wird aufgerufen, wenn eine Rede angeklickt wird (zum Bearbeiten)
     */
    fun selectSpeech(speech: Speech) {
        _viewState.value = _viewState.value.copy(selectedSpeech = speech)
    }

    /**
     * Schließt den Dialog / hebt die Auswahl auf
     */
    fun clearSelection() {
        _viewState.value = _viewState.value.copy(selectedSpeech = null)
    }

    /**
     * Aktualisiert die Filter-Query
     */
    fun updateFilterQuery(query: String) {
        _viewState.value = _viewState.value.copy(filterQuery = query)
    }

    /**
     * Schaltet die Filter-Feld-Sichtbarkeit um
     */
    fun toggleFilterVisibility() {
        _viewState.value = _viewState.value.copy(showFilterField = !_viewState.value.showFilterField)
    }

    /**
     * Schaltet die Gruppierung nach timesUsed um
     */
    fun toggleGroupByTimesUsed() {
        _viewState.value = _viewState.value.copy(groupByTimesUsed = !_viewState.value.groupByTimesUsed)
    }

    /**
     * Speichert eine Rede. Prüft vorher zur Sicherheit noch einmal die Admin-Rechte.
     */
    fun saveSpeech(speech: Speech) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            // 1. SICHERHEITSCHECK MIT POLICY
            // Wir unterscheiden: Ist es eine neue Rede (ID leer) oder ein Update?
            val isNew = speech.id.isBlank() // oder speech.id == ""

            val hasPermission = if (currentUser != null) {
                if (isNew) {
                    permissionPolicy.canCreate(currentUser)
                } else {
                    permissionPolicy.canEdit(currentUser, speech)
                }
            } else {
                false
            }

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

            // ... (Rest wie gehabt: Loading setzen, saveSpeechUseCase aufrufen) ...
            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)
            saveSpeechUseCase(speech)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isActionInProgress = false, selectedSpeech = null)
                }
                .onFailure { error ->
                    _viewState.value =
                        _viewState.value.copy(isActionInProgress = false, actionError = error.localizedMessage)
                }
        }
    }

    /**
     * Löscht eine Rede nach strenger Prüfung.
     */
    fun deleteSpeech(speechId: String) {
        viewModelScope.launch {
            // 1. Aktuellen User laden
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            // 2. Die zu löschende Rede aus dem aktuellen UI-State holen
            // Da wir reaktiv sind, haben wir die Liste meistens schon im Speicher.
            // Wir suchen die Rede in der aktuellen Liste.
            val speechToDelete = (uiState.value as? SpeechUiState.SuccessUIState)
                ?.speeches
                ?.find { it.speech.id == speechId }
                ?.speech

            // Falls die Rede im State nicht gefunden wurde (z.B. durch Race Condition),
            // brechen wir sicherheitshalber ab oder laden sie notfalls nach.
            if (speechToDelete == null) {
                _viewState.value = _viewState.value.copy(actionError = "Rede nicht gefunden.")
                return@launch
            }

            // 3. Strenge Prüfung mit der Policy und dem ECHTEN Speech-Objekt
            val hasPermission = currentUser != null && permissionPolicy.canDelete(currentUser, speechToDelete)

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung zum Löschen dieser Rede!")
                return@launch
            }

            // 4. Loading setzen
            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            // 5. Löschen ausführen
            deleteSpeechUseCase(speechId)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(
                        isActionInProgress = false,
                        selectedSpeech = null
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
private data class SpeechViewState(
    val selectedSpeech: Speech? = null,
    val isActionInProgress: Boolean = false,
    val actionError: String? = null,
    val filterQuery: String = "",
    val showFilterField: Boolean = false,
    val groupByTimesUsed: Boolean = false
)
