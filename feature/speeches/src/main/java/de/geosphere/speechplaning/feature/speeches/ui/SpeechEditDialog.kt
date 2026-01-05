package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeechEditDialog(
    speech: Speech,
    onDismiss: () -> Unit,
    onSave: (Speech) -> Unit,
    onDelete: (String) -> Unit
) {
    // Lokaler State für das Formular
    var number by remember(speech.id) { mutableStateOf(speech.number) }
    var subject by remember(speech.id) { mutableStateOf(speech.subject) }
    var active by remember(speech.id) { mutableStateOf(speech.active) }

    SpeechEditDialogContent(
        isEditMode = speech.id.isNotBlank(),
        number = number,
        subject = subject,
        active = active,
        onNumberChange = { number = it },
        onSubjectChange = { subject = it },
        onActiveChange = { active = it },
        onDismiss = onDismiss,
        onSave = {
            val sanitizedSubject = subject.trim().replace(Regex("\\s+"), " ")

            val updatedSpeech = speech.copy(
                number = number,
                subject = sanitizedSubject,
                active = active
            )
            onSave(updatedSpeech)
        },
        onDelete = { onDelete(speech.id) }
    )
}

@Composable
@Suppress("LongParameterList")
private fun SpeechEditDialogContent(
    isEditMode: Boolean,
    number: String,
    subject: String,
    active: Boolean,
    onNumberChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isEditMode) "Rede bearbeiten" else "Neue Rede",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = number,
                    onValueChange = onNumberChange,
                    label = { Text("Nummer") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    label = { Text("Thema") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
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
private fun SpeechEditDialog_AddNew_Preview() {
    SpeechPlaningTheme {
        SpeechEditDialog(
            speech = Speech(),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}

@ThemePreviews
@Composable
private fun SpeechEditDialog_Edit_Preview() {
    SpeechPlaningTheme {
        SpeechEditDialog(
            speech = Speech(
                id = "123",
                number = "142",
                subject = "Ist die Hölle ein Ort der Qualen?",
                active = true
            ),
            onDismiss = {},
            onSave = {},
            onDelete = {}
        )
    }
}
