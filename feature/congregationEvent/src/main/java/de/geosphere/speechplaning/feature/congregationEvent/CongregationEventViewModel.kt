package de.geosphere.speechplaning.feature.congregationEvent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.AppUser
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.UserRole
import de.geosphere.speechplaning.data.authentication.permission.CongregationEventPermissionPolicy
import de.geosphere.speechplaning.data.usecases.congregation.GetAllCongregationsUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.DeleteCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.GetAllCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.congregationEvent.SaveCongregationEventUseCase
import de.geosphere.speechplaning.data.usecases.speaker.GetSpeakersUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import de.geosphere.speechplaning.data.util.AppChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@Suppress("LongParameterList", "MagicNumber")
class CongregationEventViewModel(
    private val getAllCongregationEventUseCase: GetAllCongregationEventUseCase,
    private val saveCongregationEventUseCase: SaveCongregationEventUseCase,
    private val deleteCongregationEventUseCase: DeleteCongregationEventUseCase,
    private val getSpeakersUseCase: GetSpeakersUseCase,
    private val getSpeechesUseCase: GetSpeechesUseCase,
    private val getAllCongregationsUseCase: GetAllCongregationsUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: CongregationEventPermissionPolicy,
    private val appChecker: AppChecker
) : ViewModel() {

    @Suppress("VariableNaming")
    private val TAG = "CongregationEventVM"
    private val _viewState = MutableStateFlow(CongregationEventViewState())

    private val uiState: StateFlow<CongregationEventUiState> =
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

            val congregationEvents = congregationEventResult.getOrElse { emptyList() }.sortedBy { it.date }
            val allSpeakers = speakersResult.getOrElse { emptyList() }
            val allCongregations = congregationsResult?.getOrElse { emptyList() } ?: emptyList()
            val allSpeeches = speechesResult.getOrElse { emptyList() }

            // Hole das aktuellste selectedCongregationEvent aus der congregationEvents-Liste
            val selectedCongregationEventId = viewState.selectedCongregationEvent?.id
            val selectedCongregationEvent = selectedCongregationEventId?.let { eventId ->
                congregationEvents.find { it.id == eventId }
            } ?: viewState.selectedCongregationEvent

            Log.d(
                TAG,
                "selectedCongregationEventId=$selectedCongregationEventId, " +
                    "selectedCongregationEvent.isSpeakerInformed=${selectedCongregationEvent?.speakerIsInformed}"
            )

            var canCreate = false
            var canEdit = false
            var canDelete = false
            var canToggleSpeakerInformed = false

            if (appUser != null) {
                canCreate = permissionPolicy.canCreate(appUser)
                canEdit = permissionPolicy.canManageGeneral(appUser)
                canDelete = permissionPolicy.canManageGeneral(appUser)
                canToggleSpeakerInformed = permissionPolicy.canToggleSpeakerInformed(appUser)
            }

            val isWhatsAppInstalled = appChecker.isAppInstalled("com.whatsapp")

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
                canDeleteCongregationEvent = canDelete,
                canToggleSpeakerInformed = canToggleSpeakerInformed,
                isWhatsAppInstalled = isWhatsAppInstalled
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CongregationEventUiState.LoadingUiState
        )

    val filteredUiState = combine(
        uiState,
        observeCurrentUserUseCase()
    ) { currentState, appUser ->
        if (currentState is CongregationEventUiState.SuccessUiState) {
            val filteredEvents = when (appUser?.role) {
                UserRole.ADMIN -> {
                    // Admin sieht alle Termine
                    currentState.congregationEvents
                }

                UserRole.SPEAKING_ASSISTANT -> {
                    // Speaking Assistant sieht nur Termine von 2 Wochen zurück bis 5 Wochen in die Zukunft
                    val today = LocalDate.now()
                    val minDate = today.minusWeeks(2)
                    val maxDate = today.plusWeeks(9)

                    currentState.congregationEvents.filter { event ->
                        event.date?.let { date ->
                            date >= minDate && date <= maxDate
                        } ?: false
                    }
                }

                else -> {
                    // Andere Rollen (z.B. SPEAKING_PLANER, NONE) sehen alle Termine
                    currentState.congregationEvents
                }
            }

            currentState.copy(
                congregationEvents = filteredEvents
            )
        } else {
            currentState
        }
    }.stateIn(
        // Wandle den java . util . concurrent . Flow wieder in einen kotlinx . coroutines . flow .
        // StateFlow um, damit die UI ihn beobachten kann
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

    @Suppress("CyclomaticComplexMethod")
    fun saveCongregationEvent(congregationEvent: CongregationEvent) {
        viewModelScope.launch {
            val currentUser = observeCurrentUserUseCase().firstOrNull()

            val isNew = congregationEvent.id.isBlank()

            if (!hasPermissionForSave(currentUser, congregationEvent, isNew)) {
                _viewState.value = _viewState.value.copy(actionError = "Keine Berechtigung!")
                return@launch
            }

            _viewState.value = _viewState.value.copy(isActionInProgress = true, actionError = null)

            var filledEvent = congregationEvent

            filledEvent = enrichEventWithSpeakerDetails(filledEvent)

            filledEvent = enrichEventWithSpeechDetails(filledEvent)

            val result = saveCongregationEventUseCase(filledEvent)
            result
                .onSuccess { clearSelection() }
                .onFailure { error ->
                    _viewState.value =
                        _viewState.value.copy(isActionInProgress = false, actionError = error.localizedMessage)
                }
        }
    }

    private fun hasPermissionForSave(currentUser: AppUser?, congregationEvent: CongregationEvent, isNew: Boolean):
        Boolean {
        return currentUser != null && if (isNew) {
            permissionPolicy.canCreate(currentUser)
        } else {
            permissionPolicy.canEdit(currentUser, congregationEvent)
        }
    }

    private suspend fun enrichEventWithSpeakerDetails(event: CongregationEvent): CongregationEvent {
        return event.speakerId?.let { sId ->
            try {
                val speakers = getSpeakersUseCase().firstOrNull()?.getOrNull() ?: emptyList()
                val speaker = speakers.find { it.id == sId }
                if (speaker != null) {
                    val speakerName = "${speaker.firstName} ${speaker.lastName}"
                    val speakerCongId = speaker.congregationId
                    val congregations = getAllCongregationsUseCase().firstOrNull()?.getOrNull() ?: emptyList()
                    val congName = congregations.find { it.id == speakerCongId }?.name

                    event.copy(
                        speakerName = event.speakerName ?: speakerName,
                        speakerCongregationId = event.speakerCongregationId ?: speakerCongId,
                        speakerCongregationName = event.speakerCongregationName ?: congName
                    )
                } else {
                    event
                }
            } catch (e: Exception) {
                Log.w(TAG, "saveCongregationEvent: failed to enrich speaker details", e)
                event
            }
        } ?: event
    }

    private suspend fun enrichEventWithSpeechDetails(event: CongregationEvent): CongregationEvent {
        return event.speechId?.let { spId ->
            try {
                val speeches = getSpeechesUseCase().firstOrNull()?.getOrNull() ?: emptyList()
                val speech = speeches.find { it.id == spId }
                if (speech != null) {
                    event.copy(
                        speechNumber = event.speechNumber ?: speech.number,
                        speechSubject = event.speechSubject ?: speech.subject
                    )
                } else {
                    event
                }
            } catch (e: Exception) {
                Log.w(TAG, "saveCongregationEvent: failed to enrich speech details", e)
                event
            }
        } ?: event
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
