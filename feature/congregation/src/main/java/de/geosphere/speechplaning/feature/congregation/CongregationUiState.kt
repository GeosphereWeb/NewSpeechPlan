package de.geosphere.speechplaning.feature.congregation

import de.geosphere.speechplaning.core.model.Congregation

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
        val actionError: String? = null,

        // --- HIER KOMMEN DIE NEUEN FELDER HIN ---
        // Statt nur 'canEdit', splitten wir das auf:
        val canCreateCongregation: Boolean = false, // Darf neue anlegen
        val canEditCongregation: Boolean = false, // Darf existierende ändern
        val canDeleteCongregation: Boolean = false // Darf löschen (nur Admin)
    ) : CongregationUiState
}
