package de.geosphere.speechplaning.feature.congregation

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.Congregation
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
                    // 1. Die Liste der Versammlungen
                    CongregationListContent(
                        congregations = state.congregations,
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
    onSelectCongregation: (Congregation) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(congregations, key = { it.id.ifBlank { it.hashCode() } }) { congregation ->
            CongregationListItem(
                congregation = congregation,
                onClick = {
                    // Optional: Klick Verhalten definieren
                },
                onLongClick = { onSelectCongregation(congregation) }
            )
        }
    }
}

@Composable
fun CongregationListItem(congregation: Congregation, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = { Text(congregation.name) },
        supportingContent = {
            if (congregation.district.isNotBlank() || congregation.address.isNotBlank()) {
                Text("${congregation.district} - ${congregation.address}")
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
    onDismiss: () -> Unit,
    onSave: (Congregation) -> Unit,
    onDelete: (String) -> Unit
) {
    // Lokaler State für das Formular
    var name by remember(congregation.id) { mutableStateOf(congregation.name) }
    var district by remember(congregation.id) { mutableStateOf(congregation.district) }
    var address by remember(congregation.id) { mutableStateOf(congregation.address) }
    var meetingTime by remember(congregation.id) { mutableStateOf(congregation.meetingTime) }
    var active by remember(congregation.id) { mutableStateOf(congregation.active) }

    CongregationEditDialogContent(
        isEditMode = congregation.id.isNotBlank(),
        name = name,
        district = district,
        address = address,
        meetingTime = meetingTime,
        active = active,
        onNameChange = { name = it },
        onDistrictChange = { district = it },
        onAddressChange = { address = it },
        onMeetingTimeChange = { meetingTime = it },
        onActiveChange = { active = it },
        onDismiss = onDismiss,
        onSave = {
            val updatedCongregation = congregation.copy(
                name = name.trim(),
                district = district.trim(),
                address = address.trim(),
                meetingTime = meetingTime.trim(),
                active = active
            )
            onSave(updatedCongregation)
        },
        onDelete = { onDelete(congregation.id) }
    )
}

@Composable
@Suppress("LongParameterList")
private fun CongregationEditDialogContent(
    isEditMode: Boolean,
    name: String,
    district: String,
    address: String,
    meetingTime: String,
    active: Boolean,
    onNameChange: (String) -> Unit,
    onDistrictChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
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

                OutlinedTextField(
                    value = district,
                    onValueChange = onDistrictChange,
                    label = { Text("Kreis (District)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                district = "12",
                address = "Musterstraße 1, 12345 Musterstadt",
                active = true
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}
