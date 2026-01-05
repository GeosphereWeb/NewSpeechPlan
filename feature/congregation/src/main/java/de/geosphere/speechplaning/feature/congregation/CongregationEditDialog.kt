package de.geosphere.speechplaning.feature.congregation

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

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

@ThemePreviews
@Composable
private fun CongregationEditDialogContent_AddNew_Preview() {
    SpeechPlaningTheme {
        CongregationEditDialogContent(
            isEditMode = false,
            name = "",
            districtId = "",
            allDistricts = listOf(
                District(id = "1", name = "Kreis 1"),
                District(id = "2", name = "Kreis 2")
            ),
            address = "",
            meetingTime = "",
            active = true,
            onNameChange = {},
            onAddressChange = {},
            onDistrictSelected = {},
            onMeetingTimeChange = {},
            onActiveChange = {},
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
private fun CongregationEditDialogContent_Edit_Preview() {
    SpeechPlaningTheme {
        CongregationEditDialogContent(
            isEditMode = true,
            name = "Musterstadt-Nord",
            districtId = "2",
            allDistricts = listOf(
                District(id = "1", name = "Kreis 1"),
                District(id = "2", name = "Kreis 2")
            ),
            address = "Musterstraße 1, 12345 Musterstadt",
            meetingTime = "Sonntag, 10:00 Uhr",
            active = true,
            onNameChange = {},
            onAddressChange = {},
            onDistrictSelected = {},
            onMeetingTimeChange = {},
            onActiveChange = {},
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}
