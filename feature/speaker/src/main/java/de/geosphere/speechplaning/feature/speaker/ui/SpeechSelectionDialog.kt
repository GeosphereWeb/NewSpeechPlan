package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeechSelectionDialog(
    allSpeeches: List<Speech>,
    alreadySelectedIds: List<Int>,
    onSpeechSelected: (Speech) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.height(400.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vortrag auswählen", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(allSpeeches) { speech ->
                        val isAlreadySelected = speech.number.toIntOrNull() in alreadySelectedIds
                        if (!isAlreadySelected) {
                            DropdownMenuItem(
                                text = { Text("${speech.number}: ${speech.subject}") },
                                onClick = { onSpeechSelected(speech) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Schließen")
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeechSelectionDialogPreview() {
    SpeechPlaningTheme {
        SpeechSelectionDialog(
            allSpeeches = listOf(
                Speech(id = "1", number = "1", subject = "Wie gut kennst du Gott?"),
                Speech(id = "2", number = "2", subject = "Die Bibel und du"),
                Speech(id = "3", number = "3", subject = "Was ist der Sinn des Lebens?")
            ),
            alreadySelectedIds = listOf(2),
            onSpeechSelected = {},
            onDismiss = {}
        )
    }
}
