package de.geosphere.speechplaning.feature.speaker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.authentication.permission.SpeakerPermissionPolicy
import de.geosphere.speechplaning.data.usecases.congregation.ObserveAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.speaker.DeleteSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speaker.SaveSpeakerUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SpeakerUiState {
    data object LoadingUIState : SpeakerUiState
    data class ErrorUIState(val message: String) : SpeakerUiState

    data class SuccessUIState(
        val speakers: List<Speaker> = emptyList(),
        val selectedSpeaker: Speaker? = null,
        val isActionInProgress: Boolean = false,
        val actionError: String? = null,
        val canCreateSpeaker: Boolean = false,
        val canEditSpeaker: Boolean = false,
        val canDeleteSpeaker: Boolean = false,
        val allCongregations: List<Congregation> = emptyList(),
        val allSpeeches: List<Speech> = emptyList()
    ) : SpeakerUiState
}

@Suppress("LongParameterList")
class SpeakerViewModel(
    private val getSpeakersUseCase: GetSpeakersUseCase,
    private val saveSpeakerUseCase: SaveSpeakerUseCase,
    private val deleteSpeakerUseCase: DeleteSpeakerUseCase,
    private val observeAllCongregationsUseCase: ObserveAllCongregationsUseCase,
    private val getSpeechesUseCase: GetSpeechesUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: SpeakerPermissionPolicy
) : ViewModel() {

    private val defaultDistrictId = "dummy"
    private val _viewState = MutableStateFlow(SpeakerViewState())

    val uiState: StateFlow<SpeakerUiState> = combine(
        getSpeakersUseCase(),
        observeCurrentUserUseCase(),
        observeAllCongregationsUseCase(),
        getSpeechesUseCase(),
        _viewState
    ) { allSpeaker, appUser, allCongregationsResult, allSpeechesResult, viewState ->

        allSpeaker.onFailure { exception ->
            android.util.Log.e("SpeakerViewModel", "Fehler beim Laden aller Speaker", exception)
        }
        val speakersList = allSpeaker.getOrElse { emptyList() }
        val congregationsList = allCongregationsResult.getOrElse { emptyList() }

        allSpeechesResult.onFailure { exception ->
            android.util.Log.e("SpeakerViewModel", "Fehler beim Laden aller Speeches", exception)
        }
        val speechesList = allSpeechesResult.getOrElse { emptyList() }

        var canCreate = false
        var canEdit = false
        var canDelete = false

        if (appUser != null) {
            canCreate = permissionPolicy.canCreate(appUser)
            canEdit = permissionPolicy.canManageGeneral(appUser)
            canDelete = permissionPolicy.canManageGeneral(appUser)
        }

        // --- WICHTIG: Live-Update für selectedSpeaker ---
        // Wir schauen, ob wir den ausgewählten Redner in der aktuellen Live-Liste finden.
        // Falls ja, nehmen wir das frische Objekt aus der Liste.
        // Falls nein (z.B. neuer Redner, noch nicht gespeichert), bleiben wir beim lokalen State.
        val realtimeSelectedSpeaker = if (viewState.selectedSpeaker != null &&
            viewState.selectedSpeaker.id.isNotBlank()
        ) {
            speakersList.find { it.id == viewState.selectedSpeaker.id } ?: viewState.selectedSpeaker
        } else {
            viewState.selectedSpeaker
        }

        SpeakerUiState.SuccessUIState(
            speakers = speakersList.distinctBy { it.id },
            selectedSpeaker = realtimeSelectedSpeaker, // Hier das Live-Objekt nutzen
            isActionInProgress = viewState.isActionInProgress,
            actionError = viewState.actionError,
            canCreateSpeaker = canCreate,
            canEditSpeaker = canEdit,
            canDeleteSpeaker = canDelete,
            allCongregations = congregationsList,
            allSpeeches = speechesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpeakerUiState.LoadingUIState
    )

    fun selectSpeaker(speaker: Speaker) {
        _viewState.value = _viewState.value.copy(
            selectedSpeaker = speaker,
            originalSpeakerState = speaker
        )
    }

    fun clearSelection() {
        _viewState.value = _viewState.value.copy(
            selectedSpeaker = null,
            originalSpeakerState = null
        )
    }

    fun saveSpeaker(speaker: Speaker, contextCongregation: Congregation? = null) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()
            val isNew = speaker.id.isBlank()

            val hasPermission = if (currentUser != null) {
                if (isNew) {
                    permissionPolicy.canCreate(currentUser)
                } else {
                    permissionPolicy.canEdit(currentUser, speaker)
                }
            } else {
                false
            }

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            val originalSpeaker = _viewState.value.originalSpeakerState

            val result = if (contextCongregation != null) {
                saveSpeakerUseCase(speaker, contextCongregation)
            } else if (originalSpeaker != null && !isNew) {
                saveSpeakerUseCase(
                    speaker = speaker,
                    oldDistrictId = originalSpeaker.districtId,
                    oldCongregationId = originalSpeaker.congregationId
                )
            } else {
                val speakerToSave = speaker.copy(
                    districtId = speaker.districtId.ifBlank { defaultDistrictId },
                    congregationId = speaker.congregationId.ifBlank { defaultDistrictId }
                )
                saveSpeakerUseCase(speakerToSave)
            }

            result.onSuccess {
                _viewState.value = _viewState.value.copy(
                    isActionInProgress = false,
                    selectedSpeaker = null, // Schließt den Dialog nach dem Speichern (optional, falls gewünscht)
                    originalSpeakerState = null
                )
                // Hinweis: Wenn der Dialog offen bleiben soll, müsste man hier
                // selectedSpeaker NICHT auf null setzen. Da wir aber Echtzeit-Daten wollen,
                // und die Liste sich updated, ist schließen oft sauberer.
                // Wenn du willst, dass er offen bleibt, nimm die Zeile 'selectedSpeaker = null' raus.
            }
                .onFailure { error ->
                    _viewState.value =
                        _viewState.value.copy(isActionInProgress = false, actionError = error.localizedMessage)
                }
        }
    }

    fun deleteSpeaker(speakerId: String) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()
            val speakerToDelete = (uiState.value as? SpeakerUiState.SuccessUIState)
                ?.speakers
                ?.find { it.id == speakerId }

            if (speakerToDelete == null) {
                _viewState.value = _viewState.value.copy(actionError = "Redner nicht gefunden.")
                return@launch
            }

            val hasPermission = currentUser != null && permissionPolicy.canDelete(
                currentUser,
                speakerToDelete
            )

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(
                    actionError = "Keine Berechtigung zum Löschen dieses Redners!"
                )
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            val targetDistrictId = speakerToDelete.districtId.ifBlank { defaultDistrictId }

            deleteSpeakerUseCase(
                districtId = targetDistrictId,
                congregationId = speakerToDelete.congregationId,
                speakerId = speakerToDelete.id
            ).onSuccess {
                _viewState.value = _viewState.value.copy(
                    isActionInProgress = false,
                    selectedSpeaker = null,
                    originalSpeakerState = null
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

private data class SpeakerViewState(
    val selectedSpeaker: Speaker? = null,
    val originalSpeakerState: Speaker? = null,
    val isActionInProgress: Boolean = false,
    val actionError: String? = null
)
