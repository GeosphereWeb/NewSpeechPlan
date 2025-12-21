package de.geosphere.speechplaning.feature.congregationEvent

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.Event
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CongregationEventListScreen(viewModel: CongregationEventViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is CongregationEventUiState.LoadingUiState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is CongregationEventUiState.ErrorUiState -> {
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

        is CongregationEventUiState.SuccessUiState -> {
            Scaffold(
                floatingActionButton = {
                    if (state.canCreateCongregationEvent) {
                        FloatingActionButton(onClick = { viewModel.selectCongregationEvent(null) }) {
                            Icon(Icons.Default.Add, contentDescription = "Neues Ereignis")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    CongregationEventListContent(
                        congregationEvents = state.congregationEvents,
                        onSelectCongregationEvent = viewModel::selectCongregationEvent
                    )

                    if (state.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    state.actionError?.let {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }

                    if (state.showEditDialog) {
                        CongregationEventEditDialog(
                            congregationEvent = state.selectedCongregationEvent,
                            allSpeakers = state.allSpeakers,
                            allSpeeches = state.allSpeeches,
                            onDismiss = viewModel::clearSelection,
                            onSave = viewModel::saveCongregationEvent,
                            onDelete = viewModel::deleteCongregationEvent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CongregationEventListContent(
    congregationEvents: List<CongregationEvent>,
    onSelectCongregationEvent: (CongregationEvent) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(congregationEvents, key = { it.id.ifBlank { it.hashCode() } }) { event ->
            CongregationEventListItem(
                congregationEvent = event,
                onClick = { onSelectCongregationEvent(event) }, // Edit on short click
                onLongClick = { onSelectCongregationEvent(event) }
            )
        }
    }
}

@Composable
fun CongregationEventListItem(
    congregationEvent: CongregationEvent,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd. MMMM yyyy") }
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = { Text(congregationEvent.speechSubject ?: "Ereignis ohne Thema") },
        supportingContent = {
            val speakerInfo = congregationEvent.speakerName ?: "Kein Redner zugewiesen"
            Text("$speakerInfo (${congregationEvent.speakerCongregationName ?: "Unbekannt"})")
        },
        overlineContent = { Text(congregationEvent.date?.format(formatter) ?: "") },
        trailingContent = { Text(congregationEvent.speechNumber ?: "-") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CongregationEventEditDialog(
    congregationEvent: CongregationEvent?,
    allSpeakers: List<Speaker>,
    allSpeeches: List<Speech>,
    onDismiss: () -> Unit,
    onSave: (CongregationEvent) -> Unit,
    onDelete: (String) -> Unit
) {
    val isEditMode = congregationEvent != null
    val initialEvent = congregationEvent ?: CongregationEvent(dateString = LocalDate.now().toString(), eventType = Event.CONVENTION)

    var date by remember(initialEvent.id) { mutableStateOf(initialEvent.date) }
    var eventType by remember(initialEvent.id) { mutableStateOf(initialEvent.eventType) }
    var speakerId by remember(initialEvent.id) { mutableStateOf(initialEvent.speakerId) }
    var speechId by remember(initialEvent.id) { mutableStateOf(initialEvent.speechId) }
    var chairmanId by remember(initialEvent.id) { mutableStateOf<String?>(null) } // Simplified
    var notes by remember(initialEvent.id) { mutableStateOf(initialEvent.notes ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }

    // --- Abgeleiteter State: gefilterte Reden-Liste ---
    val filteredSpeeches by remember(speakerId, allSpeakers, allSpeeches) {
        derivedStateOf {
            val selectedSpeaker = allSpeakers.find { it.id == speakerId }
            if (selectedSpeaker == null) {
                allSpeeches // Wenn kein Redner, zeige alle Reden
            } else {
                val allowedSpeechNumbers = selectedSpeaker.speechNumberIds.map { it.toString() }
                allSpeeches.filter { it.number in allowedSpeechNumbers }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = if (isEditMode) "Ereignis bearbeiten" else "Neues Ereignis",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- Datums-Auswahl ---
                val formatter = remember { DateTimeFormatter.ofPattern("dd. MMMM yyyy") }
                OutlinedTextField(
                    value = date?.format(formatter) ?: "",
                    onValueChange = {}, // Read-only
                    readOnly = true,
                    label = { Text("Datum") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable { showDatePicker = true }
                )
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli())
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                                }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Abbrechen") } }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Redner-Auswahl ---
                var speakerExpanded by remember { mutableStateOf(false) }
                val selectedSpeaker = allSpeakers.find { it.id == speakerId }
                ExposedDropdownMenuBox(expanded = speakerExpanded, onExpandedChange = { speakerExpanded = it }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        readOnly = true,
                        value = selectedSpeaker?.let { "${it.lastName}, ${it.firstName}" } ?: "",
                        onValueChange = {},
                        label = { Text("Redner") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speakerExpanded) }
                    )
                    ExposedDropdownMenu(expanded = speakerExpanded, onDismissRequest = { speakerExpanded = false }) {
                        allSpeakers.forEach { speaker ->
                            DropdownMenuItem(
                                text = { Text("${speaker.lastName}, ${speaker.firstName}") },
                                onClick = {
                                    speakerId = speaker.id
                                    // Wenn der Redner gewechselt wird, prüfen wir, ob die aktuell gewählte Rede noch gültig ist
                                    val allowedSpeechNumbers = speaker.speechNumberIds.map { it.toString() }
                                    if (allSpeeches.find { s -> s.id == speechId }?.number !in allowedSpeechNumbers) {
                                        speechId = null // Rede zurücksetzen
                                    }
                                    speakerExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Rede-Auswahl (gefiltert) ---
                var speechExpanded by remember { mutableStateOf(false) }
                val selectedSpeech = filteredSpeeches.find { it.id == speechId }

                ExposedDropdownMenuBox(expanded = speechExpanded, onExpandedChange = { speechExpanded = it }) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        readOnly = true,
                        value = selectedSpeech?.let { "${it.number}: ${it.subject}" } ?: "",
                        onValueChange = {},
                        label = { Text("Rede") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speechExpanded) }
                    )
                    ExposedDropdownMenu(expanded = speechExpanded, onDismissRequest = { speechExpanded = false }) {
                        filteredSpeeches.forEach { speech ->
                            DropdownMenuItem(
                                text = { Text("${speech.number}: ${speech.subject}") },
                                onClick = {
                                    speechId = speech.id
                                    speechExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notizen") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Action Buttons ---
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (isEditMode) {
                        TextButton(
                            onClick = { onDelete(initialEvent.id) },
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
                    Button(onClick = {
                        val finalEvent = initialEvent.copy(
                            dateString = date?.toString(),
                            eventType = eventType,
                            speakerId = speakerId,
                            speechId = speechId,
                            notes = notes
                        )
                        onSave(finalEvent)
                    }) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}
