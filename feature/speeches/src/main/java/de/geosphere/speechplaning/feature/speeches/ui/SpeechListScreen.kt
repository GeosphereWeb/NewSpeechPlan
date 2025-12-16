package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.ui.atoms.SpeechListItemComposable
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeechListScreen(viewModel: SpeechViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hauptsteuerung des Screens basierend auf dem State
    when (val state = uiState) {
        is SpeechUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SpeechUiState.ErrorUIState -> {
            // Einfacher Error Screen (kann auch in eine eigene Datei ausgelagert werden)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    // Button(onClick = { viewModel.loadSpeeches() }) {
                    //     Text("Erneut versuchen")
                    // }
                }
            }
        }

        is SpeechUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    // FAB ist nur sichtbar, wenn wir Daten haben
                    if (state.canEditSpeech) {
                        FloatingActionButton(onClick = { viewModel.selectSpeech(Speech()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Speech")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    // 1. Die Liste der Reden
                    SpeechListContent(
                        speeches = state.speeches,
                        onSelectSpeech = viewModel::selectSpeech
                    )

                    // 2. Ladebalken Overlay (falls gerade gespeichert wird)
                    if (state.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    // 3. Fehler Snackbar/Text für Aktionen (falls beim Speichern etwas schief ging)
                    state.actionError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }

                    // 4. Dialog (wird angezeigt, wenn eine Rede ausgewählt ist)
                    state.selectedSpeech?.let { speech ->
                        SpeechEditDialog(
                            speech = speech,
                            onDismiss = viewModel::clearSelection,
                            onSave = viewModel::saveSpeech,
                            onDelete = viewModel::deleteSpeech
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeechListContent(
    speeches: List<Speech>,
    onSelectSpeech: (Speech) -> Unit
) {
    // Lokaler State für den Filter
    var filterQuery by remember { mutableStateOf("") }
    var visibleFilter by remember { mutableStateOf(false) }

    // Gefilterte Liste
    val filteredSpeeches = remember(speeches, filterQuery) {
        if (filterQuery.isBlank()) {
            speeches
        } else {
            speeches.filter { speech ->
                speech.number.contains(filterQuery, ignoreCase = true) ||
                    speech.subject.contains(filterQuery, ignoreCase = true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Filter Bereich ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Button(onClick = { visibleFilter = visibleFilter.not() }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Filter")
            }
            AnimatedVisibility(visibleFilter) {
                Column(modifier = Modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        label = { Text("Suche (Nummer oder Thema)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            // Optional: Lupe als Icon, falls gewünscht
                            // Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredSpeeches, key = { it.id.ifBlank { it.hashCode() } }) { speech ->
                SpeechListItem(
                    speech = speech,
                    onClick = {
                        // Optional: Klick Verhalten definieren (z.B. Details anzeigen)
                        // Aktuell leer, da Editieren über LongClick passiert?
                        // Falls Editieren bei Click passieren soll: onSelectSpeech(speech)
                    },
                    onLongClick = { onSelectSpeech(speech) }
                )
            }
        }
    }
}

@Composable
fun SpeechListItem(speech: Speech, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    SpeechListItemComposable(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        number = speech.number,
        subject = speech.subject,
        enabled = speech.active // Hier wird der Status an die UI weitergereicht
    )
}

@Composable
fun SpeechEditDialog(
    speech: Speech,
    onDismiss: () -> Unit,
    onSave: (Speech) -> Unit,
    onDelete: (String) -> Unit
) {
    // Lokaler State für das Formular
    var number by remember(speech.id) { mutableStateOf(speech.number) }
    var subject by remember(speech.id) { mutableStateOf(speech.subject) }
    var active by remember(speech.id) { mutableStateOf(speech.active) }

    SpeechEditDialogContent(
        isEditMode = speech.id.isNotBlank(),
        number = number,
        subject = subject,
        active = active,
        onNumberChange = { number = it },
        onSubjectChange = { subject = it },
        onActiveChange = { active = it },
        onDismiss = onDismiss,
        onSave = {
            val sanitizedSubject = subject.trim().replace(Regex("\\s+"), " ")

            val updatedSpeech = speech.copy(
                number = number,
                subject = sanitizedSubject,
                active = active
            )
            onSave(updatedSpeech)
        },
        onDelete = { onDelete(speech.id) }
    )
}

// Stateless Composable (Reines UI für den Dialog)
@Composable
@Suppress("LongParameterList")
private fun SpeechEditDialogContent(
    isEditMode: Boolean,
    number: String,
    subject: String,
    active: Boolean,
    onNumberChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEditMode) "Rede bearbeiten" else "Neue Rede",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = number,
                    onValueChange = onNumberChange,
                    label = { Text("Nummer") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    label = { Text("Thema") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = active, onCheckedChange = onActiveChange)
                    Text("Aktiv")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (isEditMode) {
                        TextButton(
                            onClick = onDelete,
                            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Löschen")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Button(onClick = onSave) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

// --- Previews ---

@ThemePreviews
@Composable
fun SpeechEditDialog_AddNew_Preview() {
    SpeechPlaningTheme {
        SpeechEditDialog(
            speech = Speech(),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeechEditDialog_Edit_Preview() {
    SpeechPlaningTheme {
        SpeechEditDialog(
            speech = Speech(
                id = "123",
                number = "142",
                subject = "Ist die Hölle ein Ort der Qualen?",
                active = true
            ),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeechListItemPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speech = Speech(
                id = "123",
                number = "142",
                subject = "Ist die Hölle ein Ort der Qualen?",
                active = true
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeechListItemDisabledPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speech = Speech(
                id = "124",
                number = "99",
                subject = "Dies ist eine inaktive Rede",
                active = false
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}
