package de.geosphere.speechplaning.feature.congregationEvent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.data.authentication.permission.CongregationEventPermissionPolicy
import de.geosphere.speechplaning.data.usecases.congregation.GetAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.DeleteCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.GetAllCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.SaveCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CongregationEventViewModel(
    private val getAllCongregationEventUseCase: GetAllCongregationEventUseCase,
    private val saveCongregationEventUseCase: SaveCongregationEventUseCase,
    private val deleteCongregationEventUseCase: DeleteCongregationEventUseCase,
    private val getSpeakersUseCase: GetSpeakersUseCase,
    private val getSpeechesUseCase: GetSpeechesUseCase,
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: CongregationEventPermissionPolicy
) : ViewModel() {

    private val TAG = "CongregationEventVM"
    private val _viewState = MutableStateFlow(CongregationEventViewState())

    val uiState: StateFlow<CongregationEventUiState> =
        combine(
            getAllCongregationEventUseCase(),
            getSpeechesUseCase(),
            getSpeakersUseCase(),
            observeCurrentUserUseCase(),
            _viewState
        ) { congregationEventResult, speechesResult, speakersResult, appUser, viewState ->

            // Lade zusätzlich die Congregations (combine hat nur 5-Parameter-Overloads)
            val congregationsResult = getAllCongregationsUseCase().firstOrNull()

            try {
                val size = congregationEventResult.getOrElse { emptyList<CongregationEvent>() }.size
                Log.d(TAG, "combine: received congregationEventResult size=$size")
            } catch (e: Exception) {
                Log.w(TAG, "combine: failed to log congregationEventResult", e)
            }

            val congregationEvents = congregationEventResult.getOrElse { emptyList() }
            val allSpeakers = speakersResult.getOrElse { emptyList() }
            val allCongregations = congregationsResult?.getOrElse { emptyList() } ?: emptyList()
            val allSpeeches = speechesResult.getOrElse { emptyList() }

            var canCreate = false
            var canEdit = false
            var canDelete = false

            if (appUser != null) {
                canCreate = permissionPolicy.canCreate(appUser)
                canEdit = permissionPolicy.canManageGeneral(appUser)
                canDelete = permissionPolicy.canManageGeneral(appUser)
            }

            CongregationEventUiState.SuccessUiState(
                congregationEvents = congregationEvents,
                allSpeakers = allSpeakers,
                allCongregations = allCongregations,
                allSpeeches = allSpeeches,
                selectedCongregationEvent = viewState.selectedCongregationEvent,
                showEditDialog = viewState.showEditDialog,
                isActionInProgress = viewState.isActionInProgress,
                actionError = viewState.actionError,
                canCreateCongregationEvent = canCreate,
                canEditCongregationEvent = canEdit,
                canDeleteCongregationEvent = canDelete
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CongregationEventUiState.LoadingUiState
        )

    fun selectCongregationEvent(congregationEvent: CongregationEvent?) {
        _viewState.value = _viewState.value.copy(
            selectedCongregationEvent = congregationEvent,
            showEditDialog = true
        )
    }

    fun clearSelection() {
        _viewState.value = _viewState.value.copy(
            selectedCongregationEvent = null,
            showEditDialog = false,
            isActionInProgress = false // Auch hier zurücksetzen
        )
    }

    fun saveCongregationEvent(congregationEvent: CongregationEvent) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            val isNew = congregationEvent.id.isBlank()

            val hasPermission = if (currentUser != null) {
                if (isNew) {
                    permissionPolicy.canCreate(currentUser)
                } else {
                    permissionPolicy.canEdit(currentUser, congregationEvent)
                }
            } else {
                false
            }

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            // Fülle fehlende abgeleitete Felder, falls speakerId oder speechId vorhanden ist
            var filledEvent = congregationEvent

            // Wenn speakerId vorhanden, hole Speaker und setze speakerName + speakerCongregationId/Name
            congregationEvent.speakerId?.let { sId ->
                try {
                    val speakers = getSpeakersUseCase().firstOrNull()?.getOrNull() ?: emptyList()
                    val speaker = speakers.find { it.id == sId }
                    if (speaker != null) {
                        val speakerName = "${speaker.firstName} ${speaker.lastName}"
                        val speakerCongId = speaker.congregationId
                        val congregations = getAllCongregationsUseCase().firstOrNull()?.getOrNull() ?: emptyList()
                        val congName = congregations.find { it.id == speakerCongId }?.name

                        filledEvent = filledEvent.copy(
                            speakerName = filledEvent.speakerName ?: speakerName,
                            speakerCongregationId = filledEvent.speakerCongregationId ?: speakerCongId,
                            speakerCongregationName = filledEvent.speakerCongregationName ?: congName
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "saveCongregationEvent: failed to enrich speaker details", e)
                }
            }

            // Wenn speechId vorhanden, hole Rede und setze Nummer + Subject
            congregationEvent.speechId?.let { spId ->
                try {
                    val speeches = getSpeechesUseCase().firstOrNull()?.getOrNull() ?: emptyList()
                    val speech = speeches.find { it.id == spId }
                    if (speech != null) {
                        filledEvent = filledEvent.copy(
                            speechNumber = filledEvent.speechNumber ?: speech.number,
                            speechSubject = filledEvent.speechSubject ?: speech.subject
                        )
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "saveCongregationEvent: failed to enrich speech details", e)
                }
            }

            // Aufruf der nun suspendenden UseCase
            val result = saveCongregationEventUseCase(filledEvent)
            result
                .onSuccess { clearSelection() }
                .onFailure { error ->
                    _viewState.value =
                        _viewState.value.copy(isActionInProgress = false, actionError = error.localizedMessage)
                }
        }
    }

    fun deleteCongregationEvent(congregationEventId: String) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            val eventToDelete = (uiState.value as? CongregationEventUiState.SuccessUiState)
                ?.congregationEvents
                ?.find { it.id == congregationEventId }

            if (eventToDelete == null) {
                _viewState.value = _viewState.value.copy(actionError = "Ereignis nicht gefunden.")
                return@launch
            }

            val hasPermission = currentUser != null && permissionPolicy.canDelete(currentUser, eventToDelete)

            if (!hasPermission) {
                _viewState.value = _viewState.value.copy(
                    actionError =
                    "Keine Berechtigung zum Löschen dieses Ereignisses!"
                )
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            // Nun erwartet UseCase einen einzigen eventId-Parameter
            val result = deleteCongregationEventUseCase(congregationEventId)
            result
                .onSuccess { clearSelection() }
                .onFailure { error ->
                    _viewState.value = _viewState.value.copy(
                        isActionInProgress = false,
                        actionError = error.localizedMessage ?: "Fehler beim Löschen"
                    )
                }
        }
    }
}

private data class CongregationEventViewState(
    val selectedCongregationEvent: CongregationEvent? = null,
    val showEditDialog: Boolean = false,
    val isActionInProgress: Boolean = false,
    val actionError: String? = null
)
