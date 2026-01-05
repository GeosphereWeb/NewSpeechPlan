package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeechListContent(
    speeches: List<Speech>,
    onSelectSpeech: (Speech) -> Unit
) {
    // Lokaler State für den Filter
    var filterQuery by remember { mutableStateOf("") }
    var visibleFilter by remember { mutableStateOf(false) }

    // Gefilterte Liste
    val filteredSpeeches = remember(speeches, filterQuery) {
        if (filterQuery.isBlank()) {
            speeches
        } else {
            speeches.filter { speech ->
                speech.number.contains(filterQuery, ignoreCase = true) ||
                    speech.subject.contains(filterQuery, ignoreCase = true)
            }
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
                        value = filterQuery,
                        onValueChange = { filterQuery = it },
                        label = { Text("Suche (Nummer oder Thema)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredSpeeches, key = { it.id.ifBlank { it.hashCode() } }) { speech ->
                SpeechListItem(
                    speech = speech,
                    onClick = {},
                    onLongClick = { onSelectSpeech(speech) }
                )
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeechListContentPreview() {
    SpeechPlaningTheme {
        SpeechListContent(
            speeches = listOf(
                Speech(id = "1", number = "1", subject = "Rede 1"),
                Speech(id = "2", number = "2", subject = "Rede 2"),
                Speech(id = "3", number = "3", subject = "Ein anderes Thema")
            ),
            onSelectSpeech = {}
        )
    }
}
