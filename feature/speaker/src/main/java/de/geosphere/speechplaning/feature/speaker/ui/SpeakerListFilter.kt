package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeakerListFilter(
    congregationFilter: String,
    onCongregationFilterChange: (String) -> Unit,
    speakerNameFilter: String,
    onSpeakerNameFilterChange: (String) -> Unit,
) {
    var filtersVisible by remember { mutableStateOf(false) }

    SpeakerListFilterContent(
        filtersVisible = filtersVisible,
        onToggleFilters = { filtersVisible = !filtersVisible },
        congregationFilter = congregationFilter,
        onCongregationFilterChange = onCongregationFilterChange,
        speakerNameFilter = speakerNameFilter,
        onSpeakerNameFilterChange = onSpeakerNameFilterChange
    )
}

@Composable
private fun SpeakerListFilterContent(
    filtersVisible: Boolean,
    onToggleFilters: () -> Unit,
    congregationFilter: String,
    onCongregationFilterChange: (String) -> Unit,
    speakerNameFilter: String,
    onSpeakerNameFilterChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Button(onClick = onToggleFilters) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (filtersVisible) "Filter ausblenden" else "Filter anzeigen")
            }

            AnimatedVisibility(visible = filtersVisible) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = congregationFilter,
                        onValueChange = onCongregationFilterChange,
                        label = { Text("Filter: Versammlung") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = speakerNameFilter,
                        onValueChange = onSpeakerNameFilterChange,
                        label = { Text("Filter: Redner (Vor- & Nachname)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeakerListFilterPreview() {
    SpeechPlaningTheme {
        var congregationFilter by remember { mutableStateOf("") }
        var speakerNameFilter by remember { mutableStateOf("Test") }

        SpeakerListFilter(
            congregationFilter = congregationFilter,
            onCongregationFilterChange = { congregationFilter = it },
            speakerNameFilter = speakerNameFilter,
            onSpeakerNameFilterChange = { speakerNameFilter = it }
        )
    }
}
