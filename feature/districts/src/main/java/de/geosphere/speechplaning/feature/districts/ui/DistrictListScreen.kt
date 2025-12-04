package de.geosphere.speechplaning.feature.districts.ui

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
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun DistrictListScreen(viewModel: DistrictsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DistrictUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DistrictUiState.ErrorUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Error")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is DistrictUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (state.canEditDistrict) {
                        FloatingActionButton(onClick = { viewModel.selectDistrict(District()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add District")
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    DistrictListContent(
                        districts = state.districts,
                        onSelectDistrict = viewModel::selectDistrict
                    )
                    if (state.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    state.actionError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                        )
                    }
                    state.selectedDistrict?.let { district ->
                        DistrictEditDialog(
                            district = district,
                            onDismiss = viewModel::clearSelection,
                            onSave = viewModel::saveDistrict,
                            onDelete = viewModel::deleteDistrict
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DistrictListContent(
    districts: List<District>,
    onSelectDistrict: (District) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(districts, key = { it.id }) { district ->
            DistrictListItem(
                district = district,
                onLongClick = { onSelectDistrict(district) }
            )
        }
    }
}

@Composable
fun DistrictListItem(district: District, onLongClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(district.name) },
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = onLongClick
        )
    )
}

@Composable
fun DistrictEditDialog(
    district: District,
    onDismiss: () -> Unit,
    onSave: (District) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember(district.id) { mutableStateOf(district.name) }
    val isEditMode = district.id.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEditMode) "Bezirk bearbeiten" else "Neuer Bezirk",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (isEditMode) {
                        TextButton(
                            onClick = { onDelete(district.id) },
                        ) {
                            Text("Löschen", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Abbrechen")
                    }
                    Button(onClick = {
                        val updatedDistrict = district.copy(name = name.trim())
                        onSave(updatedDistrict)
                    }) {
                        Text("Speichern")
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DistrictEditDialog_AddNew_Preview() = SpeechPlaningTheme {
    DistrictEditDialog(
        district = District(),
        onDismiss = {},
        onSave = {},
        onDelete = {}
    )
}

@ThemePreviews
@Composable
private fun DistrictEditDialog_Edit_Preview() = SpeechPlaningTheme {
    DistrictEditDialog(
        district = District(id = "123", name = "Test District"),
        onDismiss = {},
        onSave = {},
        onDelete = {}
    )
}

@ThemePreviews
@Composable
private fun DistrictListItem_Preview() = SpeechPlaningTheme {
    DistrictListItem(
        district = District(id = "123", name = "Test District"),
        onLongClick = {}
    )
}
