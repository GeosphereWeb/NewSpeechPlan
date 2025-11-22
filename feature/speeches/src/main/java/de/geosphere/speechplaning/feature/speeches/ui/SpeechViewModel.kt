package de.geosphere.speechplaning.feature.speeches.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.data.authentication.SpeechPermissionPolicy
import de.geosphere.speechplaning.data.usecases.speeches.DeleteSpeechUseCase
import de.geosphere.speechplaning.data.usecases.speeches.GetSpeechesUseCase
import de.geosphere.speechplaning.data.usecases.speeches.SaveSpeechUseCase
import de.geosphere.speechplaning.data.usecases.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpeechViewModel(
    private val getSpeechesUseCase: GetSpeechesUseCase,
    private val saveSpeechUseCase: SaveSpeechUseCase,
    private val deleteSpeechUseCase: DeleteSpeechUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val permissionPolicy: SpeechPermissionPolicy // <-- NEU: Injiziert
) : ViewModel() {

    // Lokaler State für UI-Dinge, die nicht in der DB stehen (Dialoge, Ladeanzeigen bei Aktionen)
    private val _viewState = MutableStateFlow(SpeechViewState())

    /**
     * Der UI-Status ist eine Kombination aus drei Datenströmen:
     * 1. Die Liste der Reden aus der Datenbank (GetSpeechesUseCase)
     * 2. Der aktuelle User und seine Rechte (ObserveCurrentUserUseCase)
     * 3. Der lokale View-Status (Selektion, Fehlertexte, Lade-Spinner)
     */
    val uiState: StateFlow<SpeechUiState> = combine(
        getSpeechesUseCase(), // Ruft den Flow im UseCase auf
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
        SpeechUiState.SuccessUIState(
            speeches = speechList,
            selectedSpeech = viewState.selectedSpeech,
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
                ?.find { it.id == speechId }

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
    val actionError: String? = null
)
