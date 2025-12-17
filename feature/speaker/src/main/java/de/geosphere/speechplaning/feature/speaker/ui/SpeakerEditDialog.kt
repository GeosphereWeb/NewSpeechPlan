package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

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
    var nameLast by remember(speaker.id) { mutableStateOf(speaker.lastName) }
    var nameFirst by remember(speaker.id) { mutableStateOf(speaker.firstName) }
    var congregationId by remember(speaker.id) { mutableStateOf(speaker.congregationId) }
    var districtId by remember(speaker.id) { mutableStateOf(speaker.districtId) }
    var active by remember(speaker.id) { mutableStateOf(speaker.active) }

    // StateList für die IDs.
    val selectedSpeechIds = remember(speaker.id) { speaker.speechNumberIds.toMutableStateList() }

    LaunchedEffect(speaker.speechNumberIds) {
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
            districtId = selectedCongregation.districtId
        },
        onActiveChange = { active = it },
        onAddSpeechClick = { showSpeechSelectionDialog = true },
        onRemoveSpeechClick = { idToRemove ->
            selectedSpeechIds.remove(idToRemove)
        },
        onDismiss = onDismiss,
        onSave = {
            val updatedSpeech = speaker.copy(
                firstName = nameFirst,
                lastName = nameLast,
                congregationId = congregationId,
                districtId = districtId,
                active = active,
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
                            colors = ButtonDefaults.textButtonColors(
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

@ThemePreviews
@Composable
fun SpeakerEditDialog_AddNew_Preview() {
    SpeechPlaningTheme {
        SpeakerEditDialog(
            speaker = Speaker(),
            allCongregations = listOf(
                Congregation(id = "1", name = "Musterversammlung", districtId = "D1"),
                Congregation(id = "2", name = "Zweite Versammlung", districtId = "D1")
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
                firstName = "Max",
                lastName = "Mustermann",
                active = true,
                congregationId = "1"
            ),
            allCongregations = listOf(
                Congregation(id = "1", name = "Musterversammlung", districtId = "D1")
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
