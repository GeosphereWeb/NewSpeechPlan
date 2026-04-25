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
    data object LoadingUIState : SpeechUiState
    data class ErrorUIState(val message: String) : SpeechUiState
    data class SuccessUIState(
        val speeches: List<SpeechWithUsageHistory> = emptyList(),
        val selectedSpeech: SpeechWithUsageHistory? = null, // KORRIGIERT: Muss der volle Objekttyp sein
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
    private val permissionPolicy: SpeechPermissionPolicy
) : ViewModel() {

    private val _viewState = MutableStateFlow(SpeechViewState())

    val uiState: StateFlow<SpeechUiState> = combine(
        getSpeechesWithUsageCountUseCase(),
        observeCurrentUserUseCase(),
        _viewState
    ) { speechesWithUsageResult, appUser, viewState ->

        val speechList = speechesWithUsageResult.getOrElse { emptyList() }

        val filteredList = if (viewState.filterQuery.isBlank()) {
            speechList
        } else {
            speechList.filter { speechWithUsage ->
                speechWithUsage.speech.number.contains(viewState.filterQuery, ignoreCase = true) ||
                    speechWithUsage.speech.subject.contains(viewState.filterQuery, ignoreCase = true)
            }
        }

        val groupedList = if (viewState.groupByTimesUsed) {
            filteredList
                .groupBy { it.timesUsed }
                .toSortedMap(compareBy { it })
        } else {
            null
        }

        var canCreate = false
        var canEdit = false
        var canDelete = false

        if (appUser != null) {
            canCreate = permissionPolicy.canCreate(appUser)
            canEdit = permissionPolicy.canManageGeneral(appUser)
            canDelete = permissionPolicy.canManageGeneral(appUser)
        }

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

    fun selectSpeech(speech: Speech) {
        // KORRIGIERT: Finde das vollständige SpeechWithUsageHistory-Objekt, das zur
        // übergebenen Rede gehört, und speichere dieses im State.
        val fullSpeechObject = (uiState.value as? SpeechUiState.SuccessUIState)
            ?.speeches
            ?.find { it.speech.id == speech.id }
        _viewState.value = _viewState.value.copy(selectedSpeech = fullSpeechObject)
    }

    fun clearSelection() {
        _viewState.value = _viewState.value.copy(selectedSpeech = null)
    }

    fun updateFilterQuery(query: String) {
        _viewState.value = _viewState.value.copy(filterQuery = query)
    }

    fun toggleFilterVisibility() {
        _viewState.value = _viewState.value.copy(showFilterField = !_viewState.value.showFilterField)
    }

    fun toggleGroupByTimesUsed() {
        _viewState.value = _viewState.value.copy(groupByTimesUsed = !_viewState.value.groupByTimesUsed)
    }

    fun saveSpeech(speech: Speech) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()
            val isNew = speech.id.isBlank()
            val hasPermission = if (currentUser != null) {
                if (isNew) permissionPolicy.canCreate(currentUser) else permissionPolicy.canEdit(currentUser, speech)
            } else {
                false
            }

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

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

    fun deleteSpeech(speechId: String) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()
            val speechToDelete = (uiState.value as? SpeechUiState.SuccessUIState)
                ?.speeches
                ?.find { it.speech.id == speechId }
                ?.speech

            if (speechToDelete == null) {
                _viewState.value = _viewState.value.copy(actionError = "Rede nicht gefunden.")
                return@launch
            }

            val hasPermission = currentUser != null && permissionPolicy.canDelete(currentUser, speechToDelete)

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung zum Löschen dieser Rede!")
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

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

private data class SpeechViewState(
    val selectedSpeech: SpeechWithUsageHistory? = null, // KORRIGIERT: Muss der volle Objekttyp sein
    val isActionInProgress: Boolean = false,
    val actionError: String? = null,
    val filterQuery: String = "",
    val showFilterField: Boolean = false,
    val groupByTimesUsed: Boolean = false
)
