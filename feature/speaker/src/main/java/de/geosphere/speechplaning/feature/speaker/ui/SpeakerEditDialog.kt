package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Immutable
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

@Immutable
private data class SpeakerEditDialogState(
    val isEditMode: Boolean,
    val firstName: String,
    val lastName: String,
    val congregationId: String,
    val allCongregations: List<Congregation>,
    val selectedSpeechIds: List<Int>,
    val allSpeeches: List<Speech>,
    val active: Boolean,
    val onFirstNameChange: (String) -> Unit,
    val onLastNameChange: (String) -> Unit,
    val onCongregationSelected: (Congregation) -> Unit,
    val onActiveChange: (Boolean) -> Unit,
    val onAddSpeechClick: () -> Unit,
    val onRemoveSpeechClick: (Int) -> Unit,
    val onDismiss: () -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit
)

@Composable
fun SpeakerEditDialog(
    speaker: Speaker,
    allCongregations: List<Congregation>,
    allSpeeches: List<Speech>,
    onDismiss: () -> Unit,
    onSave: (Speaker) -> Unit,
    onDelete: (String) -> Unit
) {
    var nameLast by remember(speaker.id) { mutableStateOf(speaker.lastName) }
    var nameFirst by remember(speaker.id) { mutableStateOf(speaker.firstName) }
    var congregationId by remember(speaker.id) { mutableStateOf(speaker.congregationId) }
    var districtId by remember(speaker.id) { mutableStateOf(speaker.districtId) }
    var active by remember(speaker.id) { mutableStateOf(speaker.active) }

    val selectedSpeechIds = remember(speaker.id) { speaker.speechNumberIds.toMutableStateList() }

    LaunchedEffect(speaker.speechNumberIds) {
        if (selectedSpeechIds.toList() != speaker.speechNumberIds) {
            selectedSpeechIds.clear()
            selectedSpeechIds.addAll(speaker.speechNumberIds)
        }
    }

    var showSpeechSelectionDialog by remember { mutableStateOf(false) }

    val state = SpeakerEditDialogState(
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

    SpeechEditDialogContent(state)

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
private fun SpeechEditDialogContent(state: SpeakerEditDialogState) {
    Dialog(onDismissRequest = state.onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (state.isEditMode) "Redner bearbeiten" else "Neuer Redner",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = state.onFirstNameChange,
                    label = { Text("Vorname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = state.onLastNameChange,
                    label = { Text("Nachname") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                val selectedCongregationName =
                    state.allCongregations.find { it.id == state.congregationId }?.name ?: state.congregationId

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
                        state.allCongregations.forEach { congregation ->
                            DropdownMenuItem(
                                text = { Text(congregation.name) },
                                onClick = {
                                    state.onCongregationSelected(congregation)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Zugewiesene Vorträge:", style = MaterialTheme.typography.labelLarge)
                if (state.selectedSpeechIds.isEmpty()) {
                    Text("- Keine -", style = MaterialTheme.typography.bodySmall)
                } else {
                    Column {
                        state.selectedSpeechIds.sorted().forEach { id ->
                            val speechName =
                                state.allSpeeches.find { it.number.toIntOrNull() == id }?.subject
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
                                TextButton(onClick = { state.onRemoveSpeechClick(id) }) {
                                    Text("X")
                                }
                            }
                        }
                    }
                }
                TextButton(onClick = state.onAddSpeechClick) {
                    Text("Vortrag hinzufügen")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = state.active, onCheckedChange = state.onActiveChange)
                    Text("Aktiv")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (state.isEditMode) {
                        TextButton(
                            onClick = state.onDelete,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Löschen")
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = state.onDismiss) {
                        Text("Abbrechen")
                    }
                    Button(onClick = state.onSave) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeechEditDialogContent_AddNew_Preview() {
    SpeechPlaningTheme {
        SpeechEditDialogContent(
            state = SpeakerEditDialogState(
                isEditMode = false,
                firstName = "",
                lastName = "",
                congregationId = "",
                allCongregations = listOf(
                    Congregation(id = "1", name = "Musterversammlung", districtId = "D1"),
                    Congregation(id = "2", name = "Zweite Versammlung", districtId = "D1")
                ),
                selectedSpeechIds = emptyList(),
                allSpeeches = emptyList(),
                active = true,
                onFirstNameChange = {},
                onLastNameChange = {},
                onCongregationSelected = {},
                onActiveChange = {},
                onAddSpeechClick = {},
                onRemoveSpeechClick = {},
                onDismiss = {},
                onSave = {},
                onDelete = {}
            )
        )
    }
}

@ThemePreviews
@Composable
private fun SpeechEditDialogContent_Edit_Preview() {
    SpeechPlaningTheme {
        SpeechEditDialogContent(
            state = SpeakerEditDialogState(
                isEditMode = true,
                firstName = "Max",
                lastName = "Mustermann",
                congregationId = "1",
                allCongregations = listOf(
                    Congregation(id = "1", name = "Musterversammlung", districtId = "D1")
                ),
                selectedSpeechIds = listOf(1, 2, 3),
                allSpeeches = listOf(
                    Speech(id = "1", number = "1", subject = "Wie gut kennst du Gott?"),
                    Speech(id = "2", number = "2", subject = "Die Bibel und du"),
                    Speech(id = "3", number = "3", subject = "Was ist der Sinn des Lebens?")
                ),
                active = true,
                onFirstNameChange = {},
                onLastNameChange = {},
                onCongregationSelected = {},
                onActiveChange = {},
                onAddSpeechClick = {},
                onRemoveSpeechClick = {},
                onDismiss = {},
                onSave = {},
                onDelete = {}
            )
        )
    }
}
