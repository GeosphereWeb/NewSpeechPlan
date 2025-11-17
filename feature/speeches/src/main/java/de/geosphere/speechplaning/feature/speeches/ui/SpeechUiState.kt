package de.geosphere.speechplaning.feature.speeches.ui

import de.geosphere.speechplaning.core.model.Speech

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
        val speeches: List<Speech> = emptyList(),
        val selectedSpeech: Speech? = null,
        val isActionInProgress: Boolean = false,
        val actionError: String? = null,

        // --- HIER KOMMEN DIE NEUEN FELDER HIN ---
        // Statt nur 'canEdit', splitten wir das auf:
        val canCreateSpeech: Boolean = false, // Darf neue anlegen
        val canEditSpeech: Boolean = false, // Darf existierende ändern
        val canDeleteSpeech: Boolean = false // Darf löschen (nur Admin)
    ) : SpeechUiState
}
