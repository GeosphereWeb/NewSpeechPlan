package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.SpeechWithUsageCount
import de.geosphere.speechplaning.theme.R
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeechListContent(
    speeches: List<SpeechWithUsageCount>,
    onSelectSpeech: (Speech) -> Unit
) {
    // Lokaler State für den Filter
    var filterQuery by remember { mutableStateOf("") }
    var visibleFilter by remember { mutableStateOf(false) }
    var groupFilter by remember { mutableStateOf(false) }

    // Gefilterte Liste
    val filteredSpeeches = remember(speeches, filterQuery) {
        if (filterQuery.isBlank()) {
            speeches
        } else {
            speeches.filter { speechWithUsage ->
                speechWithUsage.speech.number.contains(filterQuery, ignoreCase = true) ||
                    speechWithUsage.speech.subject.contains(filterQuery, ignoreCase = true)
            }
        }
    }

    // Gruppierte Liste nach timesUsed
    val groupedSpeeches = remember(filteredSpeeches, groupFilter) {
        if (groupFilter) {
            filteredSpeeches
                .groupBy { it.timesUsed }
                .toSortedMap(compareBy { it })
        } else {
            null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Filter Bereich ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { visibleFilter = visibleFilter.not() }) {
                    Icon(ImageVector.vectorResource(R.drawable.filter_list), contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Filter")
                }
                FilterChip(
                    modifier = Modifier.padding(8.dp),
                    onClick = { groupFilter = groupFilter.not() },
                    label = { Text("Gruppieren") },
                    selected = groupFilter,
                    leadingIcon = if (groupFilter) {
                        {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.check_small),
                                contentDescription = "Done icon",
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else {
                        null
                    },
                )
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
            if (groupFilter && groupedSpeeches != null) {
                groupedSpeeches.forEach { (timesUsed, speeches) ->
                    stickyHeader {
                        SpeechGroupHeader(timesUsed.toString())
                    }
                    items(speeches, key = { it.speech.id.ifBlank { it.hashCode() } }) { speechWithUsage ->
                        SpeechListItem(
                            speechWithUsage = speechWithUsage,
                            onClick = {},
                            onLongClick = { onSelectSpeech(speechWithUsage.speech) }
                        )
                    }
                }
            } else {
                items(filteredSpeeches, key = { it.speech.id.ifBlank { it.hashCode() } }) { speechWithUsage ->
                    SpeechListItem(
                        speechWithUsage = speechWithUsage,
                        onClick = {},
                        onLongClick = { onSelectSpeech(speechWithUsage.speech) }
                    )
                }
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
                SpeechWithUsageCount(Speech(id = "1", number = "1", subject = "Rede 1"), 3),
                SpeechWithUsageCount(Speech(id = "2", number = "2", subject = "Rede 2"), 1),
                SpeechWithUsageCount(Speech(id = "3", number = "3", subject = "Ein anderes Thema"), 5)
            ),
            onSelectSpeech = {}
        )
    }
}
