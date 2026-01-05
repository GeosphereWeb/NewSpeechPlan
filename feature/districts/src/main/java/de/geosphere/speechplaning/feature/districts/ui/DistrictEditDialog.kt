package de.geosphere.speechplaning.feature.districts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun DistrictEditDialog(
    district: District,
    onDismiss: () -> Unit,
    onSave: (District) -> Unit,
    onDelete: (String) -> Unit
) {
    var name by remember(district.id) { mutableStateOf(district.name) }

    DistrictEditDialogContent(
        isEditMode = district.id.isNotBlank(),
        name = name,
        onNameChange = { name = it },
        onDismiss = onDismiss,
        onSave = {
            val updatedDistrict = district.copy(name = name.trim())
            onSave(updatedDistrict)
        },
        onDelete = { onDelete(district.id) }
    )
}

@Composable
private fun DistrictEditDialogContent(
    isEditMode: Boolean,
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
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
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    if (isEditMode) {
                        TextButton(
                            onClick = onDelete,
                        ) {
                            Text("Löschen", color = MaterialTheme.colorScheme.error)
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

@ThemePreviews
@Composable
private fun DistrictEditDialog_AddNew_Preview() = SpeechPlaningTheme {
    DistrictEditDialogContent(
        isEditMode = false,
        name = "",
        onNameChange = {},
        onDismiss = {},
        onSave = {},
        onDelete = {}
    )
}

@ThemePreviews
@Composable
private fun DistrictEditDialog_Edit_Preview() = SpeechPlaningTheme {
    DistrictEditDialogContent(
        isEditMode = true,
        name = "Test District",
        onNameChange = {},
        onDismiss = {},
        onSave = {},
        onDelete = {}
    )
}
