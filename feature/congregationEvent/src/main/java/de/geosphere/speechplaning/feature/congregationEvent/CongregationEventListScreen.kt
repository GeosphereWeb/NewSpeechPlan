package de.geosphere.speechplaning.feature.congregationEvent

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.Event
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.text.format

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
            // NavController für Liste <-> Detail
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
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
                                events = state.congregationEvents,
                                onSelectCongregationEvent = { event ->
                                    // Klick auf Eintrag -> Detailansicht öffnen
                                    navController.navigate("details/${event.id}")
                                }
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
                                    allCongregations = state.allCongregations,
                                    allSpeeches = state.allSpeeches,
                                    onDismiss = viewModel::clearSelection,
                                    onSave = viewModel::saveCongregationEvent,
                                    onDelete = viewModel::deleteCongregationEvent
                                )
                            }
                        }
                    }
                }

                composable("details/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    val event = state.congregationEvents.find { it.id == id }
                    CongregationEventDetailsScreen(
                        congregationEvent = event,
                        onBack = { navController.popBackStack() },
                        onEdit = { ev ->
                            // Öffne Edit-Dialog über ViewModel und navigiere zurück zur Liste,
                            // dort wird der Edit-Dialog auf Basis von state.showEditDialog gerendert.
                            viewModel.selectCongregationEvent(ev)
                            navController.navigate("list")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CongregationEventListContent(
    events: List<CongregationEvent>,
    onSelectCongregationEvent: (CongregationEvent) -> Unit
) {
    // Gruppiere die CalendarContract.Events und "erinnere" dich an das Ergebnis.
    // Die Gruppierung wird nur neu berechnet, wenn sich die `events`-Liste ändert.
    val groupedEvents = remember(events) {
        events
            .sortedByDescending { it.date }
            .groupBy { it.date?.year ?: 0 } // 1. Gruppierung nach Jahr
            .mapValues { entry ->
                entry.value.groupBy { it.date?.month ?: java.time.Month.JANUARY } // 2. Gruppierung nach Monat
            }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Die Logik für die Sticky Header bleibt dieselbe wie im ursprünglichen Vorschlag
        groupedEvents.forEach { (year, eventsByMonth) ->

            // --- Sticky Header für das Jahr ---
            stickyHeader {
                YearHeader(year = year)
            }

            eventsByMonth.forEach { (month, eventsInMonth) ->

                // --- Sticky Header für den Monat ---
                stickyHeader {
                    MonthHeader(month = month.toString(), year = year)
                }

                // Die Items für den aktuellen Monat
                items(eventsInMonth, key = { it.id.ifBlank { it.hashCode() } }) { event ->
                    CongregationEventListItem(
                        congregationEvent = event,
                        onClick = { onSelectCongregationEvent(event) }, // Öffnet Details
                        onLongClick = null
                    )
                }
            }
        }
    }
}

@Composable
fun YearHeader(year: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer, // Eine kräftige Hintergrundfarbe
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.titleLarge, // Große Schrift für die Jahreszahl
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * Ein Composable für den "Sticky Header", der den Monat anzeigt.
 * Er ist optisch dem Jahres-Header untergeordnet.
 *
 * @param month Der anzuzeigende Monat als [java.time.Month].
 */
@Composable
fun MonthHeader(month: String, year: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant, // Eine dezentere Hintergrundfarbe
        modifier = Modifier.fillMaxWidth()
    ) {
        // Formatiert den Monatsnamen, z.B. "Oktober"
        val monthString = remember(month) {
            // Formatter, der nur den vollen Monatsnamen ausgibt
            val formatter = DateTimeFormatter.ofPattern("MMMM")
            month.format(formatter)
        }

        Text(
            text = monthString,
            style = MaterialTheme.typography.titleMedium, // Etwas kleinere Schrift als das Jahr
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp) // Eingerückt, um die Hierarchie zu zeigen
        )
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
    allCongregations: List<Congregation>,
    allSpeeches: List<Speech>,
    onDismiss: () -> Unit,
    onSave: (CongregationEvent) -> Unit,
    onDelete: (String) -> Unit
) {
    val isEditMode = congregationEvent != null
    val initialEvent = congregationEvent ?: CongregationEvent(
        dateString = LocalDate.now().toString(),
        eventType = Event.CONVENTION
    )

    var date by remember(initialEvent.id) { mutableStateOf(initialEvent.date) }
    var eventType by remember(initialEvent.id) { mutableStateOf(initialEvent.eventType) }
    var speakerId by remember(initialEvent.id) { mutableStateOf(initialEvent.speakerId) }
    var speechId by remember(initialEvent.id) { mutableStateOf(initialEvent.speechId) }
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
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = {
                            showDatePicker = true
                        }) { Icon(Icons.Default.CalendarToday, contentDescription = "Datum wählen") }
                    }
                )

                if (showDatePicker) {
                    val context = LocalContext.current
                    val initial = date ?: LocalDate.now()
                    // DisposableEffect stellt sicher, dass der Dialog korrekt angezeigt und wieder geschlossen wird
                    DisposableEffect(key1 = showDatePicker) {
                        val dp = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                date = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            initial.year,
                            initial.monthValue - 1,
                            initial.dayOfMonth
                        )
                        dp.setOnDismissListener { showDatePicker = false }
                        dp.show()

                        onDispose {
                            try {
                                dp.dismiss()
                            } catch (_: Exception) {
                                // ignore
                            }
                        }
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
                    DropdownMenu(expanded = speakerExpanded, onDismissRequest = { speakerExpanded = false }) {
                        allSpeakers.forEach { speaker ->
                            DropdownMenuItem(
                                text = { Text("${speaker.lastName}, ${speaker.firstName}") },
                                onClick = {
                                    speakerId = speaker.id
                                    // Wenn der Redner gewechselt wird, prüfen wir, ob die aktuell gewählte Rede noch
                                    // gültig ist
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
                    DropdownMenu(expanded = speechExpanded, onDismissRequest = { speechExpanded = false }) {
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
                        // Erzeuge finalen Event mit vollständigen Feldern
                        val selectedSpeaker = allSpeakers.find { it.id == speakerId }
                        val selectedSpeech = allSpeeches.find { it.id == speechId }
                        val speakerName = selectedSpeaker?.let { "${it.firstName} ${it.lastName}" }
                        val speechNumber = selectedSpeech?.number
                        val speechSubject = selectedSpeech?.subject
                        val speakerCongregationId = selectedSpeaker?.congregationId
                        val speakerCongregationName = allCongregations.find { it.id == speakerCongregationId }?.name

                        val finalEvent = initialEvent.copy(
                            dateString = date?.toString(),
                            eventType = eventType,
                            speakerId = speakerId,
                            speakerName = speakerName,
                            speakerCongregationId = speakerCongregationId,
                            speakerCongregationName = speakerCongregationName,
                            speechId = speechId,
                            speechNumber = speechNumber,
                            speechSubject = speechSubject,
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

@Composable
fun CongregationEventDetailsScreen(
    congregationEvent: CongregationEvent?,
    onBack: () -> Unit,
    onEdit: (CongregationEvent?) -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Ereignis-Details", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            if (congregationEvent == null) {
                Text("Ereignis nicht gefunden")
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("Zurück") }
                return@Column
            }

            val formatter = remember { DateTimeFormatter.ofPattern("dd. MMMM yyyy") }
            Text("Datum: ${congregationEvent.date?.format(formatter) ?: congregationEvent.dateString}")
            Text("Typ: ${congregationEvent.eventType}")
            Text("Redner: ${congregationEvent.speakerName ?: "-"}")
            Text("Gemeinde: ${congregationEvent.speakerCongregationName ?: "-"}")
            Text("Rede: ${congregationEvent.speechNumber ?: "-"} ${congregationEvent.speechSubject ?: ""}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Notizen:")
            Text(congregationEvent.notes ?: "")

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onBack) { Text("Zurück") }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = { onEdit(congregationEvent) }) { Text("Bearbeiten") }
            }
        }
    }
}
