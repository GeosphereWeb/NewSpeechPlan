package de.geosphere.speechplaning.feature.congregation

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun CongregationListScreen(viewModel: CongregationViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hauptsteuerung des Screens basierend auf dem State
    when (val state = uiState) {
        is CongregationUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is CongregationUiState.ErrorUIState -> {
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
                }
            }
        }

        is CongregationUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    // FAB ist nur sichtbar, wenn wir Daten haben
                    if (state.canCreateCongregation) {
                        FloatingActionButton(onClick = { viewModel.selectCongregation(Congregation()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Congregation")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    // 1. Die Liste der Versammlungen mit Filter
                    CongregationListContent(
                        congregations = state.congregations,
                        allDistricts = state.allDistricts,
                        onSelectCongregation = viewModel::selectCongregation
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

                    // 4. Dialog (wird angezeigt, wenn eine Versammlung ausgewählt ist)
                    state.selectedCongregation?.let { congregation ->
                        CongregationEditDialog(
                            congregation = congregation,
                            allDistricts = state.allDistricts,
                            onDismiss = viewModel::clearSelection,
                            onSave = viewModel::saveCongregation,
                            onDelete = viewModel::deleteCongregation
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CongregationListContent(
    congregations: List<Congregation>,
    allDistricts: List<District>,
    onSelectCongregation: (Congregation) -> Unit
) {
    // State für die Filter
    var districtFilter by remember { mutableStateOf("") }
    var congregationNameFilter by remember { mutableStateOf("") }
    var visibleFilter by remember { mutableStateOf(false) }

    // Gefilterte Liste
    val filteredCongregations = remember(congregations, allDistricts, districtFilter, congregationNameFilter) {
        congregations.filter { cong ->
            // Filter nach Versammlung Name
            val matchesCongName = if (congregationNameFilter.isBlank()) {
                true
            } else {
                cong.name.contains(congregationNameFilter, ignoreCase = true)
            }

            // Filter nach District Name
            val matchesDistrict = if (districtFilter.isBlank()) {
                true
            } else {
                val distName = allDistricts.find { it.id == cong.districtId }?.name ?: ""
                distName.contains(districtFilter, ignoreCase = true)
            }

            matchesCongName && matchesDistrict
        }
    }

    // Gruppieren
    val grouped = remember(filteredCongregations, allDistricts) {
        filteredCongregations
            .groupBy { it.districtId }
            .toList()
            .sortedBy { (districtId, _) ->
                allDistricts.find { it.id == districtId }?.name ?: districtId
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
                        value = districtFilter,
                        onValueChange = { districtFilter = it },
                        label = { Text("Filter: Kreis") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = congregationNameFilter,
                        onValueChange = { congregationNameFilter = it },
                        label = { Text("Filter: Versammlung") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            grouped.forEach { (districtId, congregationsInDistrict) ->
                val districtName = allDistricts.find { it.id == districtId }?.name ?: districtId

                stickyHeader { CongregationDistrictHeader(districtName = districtName) }

                items(congregationsInDistrict, key = { it.id.ifBlank { it.hashCode() } }) { congregation ->
                    CongregationListItem(
                        congregation = congregation,
                        districtName = districtName,
                        onClick = {
                            // Optional: Klick Verhalten definieren
                        },
                        onLongClick = { onSelectCongregation(congregation) }
                    )
                }
            }
        }
    }
}

@Composable
fun CongregationDistrictHeader(districtName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant) // Hintergrundfarbe zur Abhebung
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = if (districtName.isBlank()) "Ohne Kreiszuordnung" else "Kreis $districtName",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Composable
fun CongregationListItem(
    congregation: Congregation,
    districtName: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?
) {
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = { Text(congregation.name) },
        supportingContent = {
            val displayDistrict = districtName.ifBlank { congregation.districtId }
            if (displayDistrict.isNotBlank() || congregation.address.isNotBlank()) {
                val sb = StringBuilder()
                if (displayDistrict.isNotBlank()) sb.append(displayDistrict)
                if (displayDistrict.isNotBlank() && congregation.address.isNotBlank()) sb.append(" - ")
                if (congregation.address.isNotBlank()) sb.append(congregation.address)
                Text(sb.toString())
            }
        },
        trailingContent = {
            if (!congregation.active) {
                Text("Inaktiv", color = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
fun CongregationEditDialog(
    congregation: Congregation,
    allDistricts: List<District>,
    onDismiss: () -> Unit,
    onSave: (Congregation) -> Unit,
    onDelete: (String) -> Unit
) {
    // Lokaler State für das Formular
    var name by remember(congregation.id) { mutableStateOf(congregation.name) }
    var address by remember(congregation.id) { mutableStateOf(congregation.address) }
    var districtId by remember(congregation.id) { mutableStateOf(congregation.districtId) }
    var meetingTime by remember(congregation.id) { mutableStateOf(congregation.meetingTime) }
    var active by remember(congregation.id) { mutableStateOf(congregation.active) }

    CongregationEditDialogContent(
        isEditMode = congregation.id.isNotBlank(),
        name = name,
        districtId = districtId,
        allDistricts = allDistricts,
        address = address,
        meetingTime = meetingTime,
        active = active,
        onNameChange = { name = it },
        onDistrictSelected = { selectedDistrict ->
            districtId = selectedDistrict.id
        },
        onAddressChange = { address = it },
        onMeetingTimeChange = { meetingTime = it },
        onActiveChange = { active = it },
        onDismiss = onDismiss,
        onSave = {
            val updatedCongregation = congregation.copy(
                name = name.trim(),
                districtId = districtId.trim(),
                address = address.trim(),
                meetingTime = meetingTime.trim(),
                active = active
            )
            onSave(updatedCongregation)
        },
        onDelete = { onDelete(congregation.id) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList", "LongMethod")
private fun CongregationEditDialogContent(
    isEditMode: Boolean,
    name: String,
    districtId: String,
    allDistricts: List<District>,
    address: String,
    meetingTime: String,
    active: Boolean,
    onNameChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDistrictSelected: (District) -> Unit,
    onMeetingTimeChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEditMode) "Versammlung bearbeiten" else "Neue Versammlung",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                val selectedDistrictName =
                    allDistricts.find { it.id == districtId }?.name ?: districtId

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth(),
                        readOnly = true,
                        value = selectedDistrictName,
                        onValueChange = {},
                        label = { Text("Kreis") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allDistricts.forEach { district ->
                            DropdownMenuItem(
                                text = { Text(district.name) },
                                onClick = {
                                    onDistrictSelected(district)
                                    expanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text("Adresse") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = meetingTime,
                    onValueChange = onMeetingTimeChange,
                    label = { Text("Zusammenkunftszeit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
fun CongregationEditDialog_AddNew_Preview() {
    SpeechPlaningTheme {
        CongregationEditDialog(
            congregation = Congregation(),
            allDistricts = listOf(
                District(id = "1", name = "Kreis 1"),
                District(id = "2", name = "Kreis 2")
            ),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
fun CongregationListItemPreview() {
    SpeechPlaningTheme {
        CongregationListItem(
            congregation = Congregation(
                id = "123",
                name = "Musterstadt-Nord",
                districtId = "12",
                address = "Musterstraße 1, 12345 Musterstadt",
                active = true
            ),
            districtName = "Kreis 12",
            onClick = {},
            onLongClick = {}
        )
    }
}
