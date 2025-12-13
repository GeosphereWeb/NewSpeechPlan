package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.ui.atoms.SpeechListItemComposable
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeakerListScreen(viewModel: SpeakerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SpeakerUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SpeakerUiState.ErrorUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        is SpeakerUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (state.canEditSpeaker) {
                        FloatingActionButton(onClick = { viewModel.selectSpeaker(Speaker()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Speaker")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    SpeakerListContent(
                        speaker = state.speakers,
                        allCongregations = state.allCongregations,
                        onSelectSpeaker = viewModel::selectSpeaker
                    )

                    if (state.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    state.actionError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }

                    state.selectedSpeaker?.let { speaker ->
                        SpeakerEditDialog(
                            speaker = speaker,
                            allCongregations = state.allCongregations,
                            allSpeeches = state.allSpeeches,
                            onDismiss = viewModel::clearSelection,
                            onSave = viewModel::saveSpeaker,
                            onDelete = viewModel::deleteSpeaker
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeakerEditDialog(
    speaker: Speaker,
    allCongregations: List<Congregation>,
    allSpeeches: List<Speech>,
    onDismiss: () -> Unit,
    onSave: (Speaker) -> Unit,
    onDelete: (String) -> Unit
) {
    // State initialisieren
    var number by remember(speaker.id) { mutableStateOf(speaker.id) }
    var nameLast by remember(speaker.id) { mutableStateOf(speaker.nameLast) }
    var nameFirst by remember(speaker.id) { mutableStateOf(speaker.nameFirst) }
    var congregationId by remember(speaker.id) { mutableStateOf(speaker.congregationId) }
    var districtId by remember(speaker.id) { mutableStateOf(speaker.districtId) }
    var active by remember(speaker.id) { mutableStateOf(speaker.isActive) }

    // StateList für die IDs.
    // Wir initialisieren sie einmalig beim ersten Aufruf oder ID-Wechsel.
    val selectedSpeechIds = remember(speaker.id) { speaker.speechNumberIds.toMutableStateList() }

    // WICHTIG: Synchronisation bei Updates von Firestore!
    // Wenn 'speaker.speechNumberIds' sich ändert (z.B. durch Firestore-Update im Hintergrund),
    // müssen wir unsere lokale Arbeitsliste 'selectedSpeechIds' aktualisieren, damit der User das sieht.
    LaunchedEffect(speaker.speechNumberIds) {
        // Wir vergleichen, um unnötige UI-Updates zu vermeiden
        if (selectedSpeechIds.toList() != speaker.speechNumberIds) {
            selectedSpeechIds.clear()
            selectedSpeechIds.addAll(speaker.speechNumberIds)
        }
    }

    var showSpeechSelectionDialog by remember { mutableStateOf(false) }

    SpeechEditDialogContent(
        isEditMode = speaker.id.isNotBlank(),
        firstName = nameFirst,
        lastName = nameLast,
        congregationId = congregationId,
        allCongregations = allCongregations,
        selectedSpeechIds = selectedSpeechIds,
        allSpeeches = allSpeeches,
        active = active,
        onFirstNameChange = { nameFirst = it },
        onLastNameChange = { nameLast = it },
        onCongregationSelected = { selectedCongregation ->
            congregationId = selectedCongregation.id
            districtId = selectedCongregation.district
        },
        onActiveChange = { active = it },
        onAddSpeechClick = { showSpeechSelectionDialog = true },
        onRemoveSpeechClick = { idToRemove ->
            selectedSpeechIds.remove(idToRemove)
        },
        onDismiss = onDismiss,
        onSave = {
            val updatedSpeech = speaker.copy(
                nameFirst = nameFirst,
                nameLast = nameLast,
                congregationId = congregationId,
                districtId = districtId,
                isActive = active,
                speechNumberIds = selectedSpeechIds.toList()
            )
            onSave(updatedSpeech)
        },
        onDelete = { onDelete(speaker.id) }
    )

    if (showSpeechSelectionDialog) {
        SpeechSelectionDialog(
            allSpeeches = allSpeeches,
            alreadySelectedIds = selectedSpeechIds,
            onSpeechSelected = { speech ->
                val speechNum = speech.number.toIntOrNull()
                if (speechNum != null && !selectedSpeechIds.contains(speechNum)) {
                    selectedSpeechIds.add(speechNum)
                }
            },
            onDismiss = { showSpeechSelectionDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
private fun SpeechEditDialogContent(
    isEditMode: Boolean,
    firstName: String,
    lastName: String,
    congregationId: String,
    allCongregations: List<Congregation>,
    selectedSpeechIds: List<Int>,
    allSpeeches: List<Speech>,
    active: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onCongregationSelected: (Congregation) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onAddSpeechClick: () -> Unit,
    onRemoveSpeechClick: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEditMode) "Redner bearbeiten" else "Neuer Redner",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = onFirstNameChange,
                    label = { Text("Vorname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lastName,
                    onValueChange = onLastNameChange,
                    label = { Text("Nachname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                val selectedCongregationName =
                    allCongregations.find { it.id == congregationId }?.name ?: congregationId

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .fillMaxWidth(),
                        readOnly = true,
                        value = selectedCongregationName,
                        onValueChange = {},
                        label = { Text("Versammlung") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allCongregations.forEach { congregation ->
                            DropdownMenuItem(
                                text = { Text(congregation.name) },
                                onClick = {
                                    onCongregationSelected(congregation)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Zugewiesene Vorträge:", style = MaterialTheme.typography.labelLarge)
                if (selectedSpeechIds.isEmpty()) {
                    Text("- Keine -", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column {
                        selectedSpeechIds.sorted().forEach { id ->
                            val speechName =
                                allSpeeches.find { it.number.toIntOrNull() == id }?.subject
                                    ?: "Unbekannter Vortrag"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$id: $speechName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { onRemoveSpeechClick(id) }) {
                                    Text("X")
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = onAddSpeechClick) {
                    Text("Vortrag hinzufügen")
                }

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

@Composable
fun SpeechSelectionDialog(
    allSpeeches: List<Speech>,
    alreadySelectedIds: List<Int>,
    onSpeechSelected: (Speech) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.height(400.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vortrag auswählen", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allSpeeches) { speech ->
                        val isAlreadySelected = speech.number.toIntOrNull() in alreadySelectedIds
                        if (!isAlreadySelected) {
                            DropdownMenuItem(
                                text = { Text("${speech.number}: ${speech.subject}") },
                                onClick = { onSpeechSelected(speech) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Schließen")
                }
            }
        }
    }
}

@Composable
fun SpeakerListContent(
    speaker: List<Speaker>,
    allCongregations: List<Congregation>,
    onSelectSpeaker: (Speaker) -> Unit
) {
    val grouped = remember(speaker) {
        speaker
            .groupBy { speaker ->
                allCongregations.find { it.id == speaker.congregationId }?.name
                    ?: if (speaker.congregationId.isBlank()) {
                        "Keine Versammlung"
                    } else {
                        "Unbekannte ID: " +
                            speaker.congregationId
                    }
            }.toSortedMap()
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        grouped.forEach { (congregationName, speakersInGroup) ->
// 2. Der Sticky Header (Name der Versammlung)
            stickyHeader {
                SpeakerGroupHeader(title = congregationName)
            }

            items(speakersInGroup, key = { it.id.ifBlank { it.hashCode() } }) { speaker ->
                SpeakerListItem(
                    speaker = speaker,
                    onClick = { },
                    onLongClick = { onSelectSpeaker(speaker) }
                )
            }
        }
    }
}

@Composable
fun SpeakerListItem(speaker: Speaker, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    SpeechListItemComposable(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        number = "",
        subject = "${speaker.nameLast}, ${speaker.nameFirst}",
        enabled = speaker.isActive
    )
}

@Composable
fun SpeakerGroupHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            // Fallback, falls mal kein Versammlungsname eingetragen ist
            text = if (title.isBlank()) "Keine Versammlung zugeordnet" else title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@ThemePreviews
@Composable
fun SpeakerEditDialog_AddNew_Preview() {
    SpeechPlaningTheme {
        SpeakerEditDialog(
            speaker = Speaker(),
            allCongregations = listOf(
                Congregation(id = "1", name = "Musterversammlung", district = "D1"),
                Congregation(id = "2", name = "Zweite Versammlung", district = "D1")
            ),
            allSpeeches = emptyList(),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeakerEditDialog_Edit_Preview() {
    SpeechPlaningTheme {
        SpeakerEditDialog(
            speaker = Speaker(
                id = "123",
                nameFirst = "Max",
                nameLast = "Mustermann",
                isActive = true,
                congregationId = "1"
            ),
            allCongregations = listOf(
                Congregation(id = "1", name = "Musterversammlung", district = "D1")
            ),
            allSpeeches = listOf(
                Speech(id = "1", number = "1", subject = "Wie gut kennst du Gott?")
            ),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeakerListItemPreview() {
    SpeechPlaningTheme {
        SpeakerListItem(
            speaker = Speaker(
                id = "123",
                nameFirst = "Max",
                nameLast = "Mustermann",
                isActive = true
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}
