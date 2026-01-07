package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import java.time.LocalDate

/**
 * Data class für den State des Edit Dialogs
 */
data class CongregationEventEditDialogState(
    val date: LocalDate?,
    val speakerId: String?,
    val speechId: String?,
    val notes: String
)

/**
 * Stateless Composable für den Inhalt des Edit Dialogs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CongregationEventEditDialogContent(
    state: CongregationEventEditDialogState,
    filteredSpeeches: List<Speech>,
    allSpeakers: List<Speaker>,
    isEditMode: Boolean,
    onDateChange: (LocalDate?) -> Unit,
    onSpeakerChange: (String) -> Unit,
    onSpeechChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    stringProvider: AppEventStringProvider
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isEditMode) "Ereignis bearbeiten" else "Neues Ereignis",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                DateSelector(
                    date = state.date,
                    onClick = onDateChange
                )

                Spacer(modifier = Modifier.height(8.dp))

                SpeakerSelector(
                    allSpeakers = allSpeakers,
                    selectedSpeakerId = state.speakerId,
                    onSpeakerSelected = onSpeakerChange
                )

                Spacer(modifier = Modifier.height(8.dp))

                SpeechSelector(
                    filteredSpeeches = filteredSpeeches,
                    selectedSpeechId = state.speechId,
                    onSpeechSelected = onSpeechChange
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notizen") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isEditMode) {
                        Button(
                            onClick = onDelete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Löschen")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(1f)) {
                        TextButton(onClick = onDismiss) {
                            Text("Abbrechen")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onSave) {
                            Text("Speichern")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    date: LocalDate?,
    onClick: (LocalDate?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("dd. MMMM yyyy") }
    OutlinedTextField(
        value = date?.format(formatter) ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text("Datum") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Datum wählen")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date?.atStartOfDay(
                java.time.ZoneId.systemDefault()
            )?.toInstant()?.toEpochMilli()
        )
        val coroutineScope = rememberCoroutineScope()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val selectedDate = java.time.Instant.ofEpochMilli(it)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            onClick(selectedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerSelector(
    allSpeakers: List<Speaker>,
    selectedSpeakerId: String?,
    onSpeakerSelected: (String) -> Unit
) {
    var speakerExpanded by remember { mutableStateOf(false) }
    val selectedSpeaker = allSpeakers.find { it.id == selectedSpeakerId }

    ExposedDropdownMenuBox(
        expanded = speakerExpanded,
        onExpandedChange = { speakerExpanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
            readOnly = true,
            value = selectedSpeaker?.let { "${it.lastName}, ${it.firstName}" } ?: "",
            onValueChange = {},
            label = { Text("Redner") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speakerExpanded) }
        )
        ExposedDropdownMenu(
            expanded = speakerExpanded,
            onDismissRequest = { speakerExpanded = false }
        ) {
            allSpeakers.forEach { speaker ->
                DropdownMenuItem(
                    text = { Text("${speaker.lastName}, ${speaker.firstName}") },
                    onClick = {
                        onSpeakerSelected(speaker.id)
                        speakerExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechSelector(
    filteredSpeeches: List<Speech>,
    selectedSpeechId: String?,
    onSpeechSelected: (String) -> Unit
) {
    var speechExpanded by remember { mutableStateOf(false) }
    val selectedSpeech = filteredSpeeches.find { it.id == selectedSpeechId }

    ExposedDropdownMenuBox(
        expanded = speechExpanded,
        onExpandedChange = { speechExpanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
            readOnly = true,
            value = selectedSpeech?.let { "#${it.number} - ${it.subject}" } ?: "",
            onValueChange = {},
            label = { Text("Rede") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speechExpanded) }
        )

        ExposedDropdownMenu(
            expanded = speechExpanded,
            onDismissRequest = { speechExpanded = false }
        ) {
            filteredSpeeches.forEach { speech ->
                DropdownMenuItem(
                    text = { Text("#${speech.number} - ${speech.subject}") },
                    onClick = {
                        onSpeechSelected(speech.id)
                        speechExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * Stateful Composable für den Edit Dialog (ursprüngliche Implementierung)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CongregationEventEditDialog(
    congregationEvent: CongregationEvent?,
    allSpeakers: List<Speaker>,
    allCongregations: List<Congregation>,
    allSpeeches: List<Speech>,
    onDismiss: () -> Unit,
    onSave: (CongregationEvent) -> Unit,
    onDelete: (String) -> Unit,
    stringProvider: AppEventStringProvider
) {
    val isEditMode = congregationEvent != null
    val initialEvent = congregationEvent ?: CongregationEvent(
        dateString = LocalDate.now().toString(),
        eventType = Event.CONVENTION
    )

    var dialogState by remember(initialEvent.id) {
        mutableStateOf(
            CongregationEventEditDialogState(
                date = initialEvent.date,
                speakerId = initialEvent.speakerId,
                speechId = initialEvent.speechId,
                notes = initialEvent.notes ?: ""
            )
        )
    }

    val filteredSpeeches by remember(dialogState.speakerId, allSpeakers, allSpeeches) {
        androidx.compose.runtime.derivedStateOf {
            val selectedSpeaker = allSpeakers.find { it.id == dialogState.speakerId }
            if (selectedSpeaker == null) {
                allSpeeches
            } else {
                val allowedSpeechNumbers = selectedSpeaker.speechNumberIds.map { it.toString() }
                allSpeeches.filter { it.number in allowedSpeechNumbers }
            }
        }
    }

    CongregationEventEditDialogContent(
        state = dialogState,
        filteredSpeeches = filteredSpeeches,
        allSpeakers = allSpeakers,
        isEditMode = isEditMode,
        onDateChange = { dialogState = dialogState.copy(date = it) },
        onSpeakerChange = { newSpeakerId ->
            dialogState = dialogState.copy(speakerId = newSpeakerId)
            val allowedSpeechNumbers = allSpeakers.find { it.id == newSpeakerId }?.speechNumberIds?.map { it.toString() }
                ?: emptyList()
            if (allSpeeches.find { s -> s.id == dialogState.speechId }?.number !in allowedSpeechNumbers) {
                dialogState = dialogState.copy(speechId = null)
            }
        },
        onSpeechChange = { dialogState = dialogState.copy(speechId = it) },
        onNotesChange = { dialogState = dialogState.copy(notes = it) },
        onDismiss = onDismiss,
        onSave = {
            val finalEvent = initialEvent.copy(
                dateString = dialogState.date?.toString(),
                speakerId = dialogState.speakerId,
                speechId = dialogState.speechId,
                notes = dialogState.notes
            )
            onSave(finalEvent)
        },
        onDelete = { onDelete(initialEvent.id) },
        stringProvider = stringProvider
    )
}


@ThemePreviews
@Composable
fun CongregationEventEditDialogContentPreview() = SpeechPlaningTheme {
    val dialogState = CongregationEventEditDialogState(
        date = LocalDate.now(),
        speakerId = "speaker1",
        speechId = "speech1",
        notes = "Notizen für das Ereignis"
    )
    val mockSpeakers = listOf(
        Speaker(id = "speaker1", firstName = "Max", lastName = "Müller", speechNumberIds = listOf(1, 2, 3))
    )
    val mockSpeeches = listOf(
        Speech(id = "speech1", number = "1", subject = "Vortrag über Glauben")
    )

    CongregationEventEditDialogContent(
        state = dialogState,
        filteredSpeeches = mockSpeeches,
        allSpeakers = mockSpeakers,
        isEditMode = true,
        onDateChange = {},
        onSpeakerChange = {},
        onSpeechChange = {},
        onNotesChange = {},
        onDismiss = {},
        onSave = {},
        onDelete = {},
        stringProvider = AppEventStringProvider(LocalContext.current)
    )
}
