package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.SpeechWithUsageHistory
import de.geosphere.speechplaning.theme.R
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeechListContent(
    speeches: List<SpeechWithUsageHistory>,
    filterQuery: String,
    onFilterQueryChange: (String) -> Unit,
    showFilterField: Boolean,
    onToggleFilterVisibility: () -> Unit,
    groupByTimesUsed: Boolean,
    onToggleGroupByTimesUsed: () -> Unit,
    filteredSpeeches: List<SpeechWithUsageHistory>,
    groupedSpeeches: Map<Int, List<SpeechWithUsageHistory>>?,
    onSelectSpeech: (Speech) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // --- Filter Bereich ---
        Column(modifier = Modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    modifier = Modifier,
                    onClick = { onToggleGroupByTimesUsed() },
                    label = { Text("Gruppieren") },
                    selected = groupByTimesUsed,
                    leadingIcon = if (groupByTimesUsed) {
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
                Button(onClick = { onToggleFilterVisibility() }) {
                    Icon(ImageVector.vectorResource(R.drawable.filter_list), contentDescription = "Filter")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Filter")
                }
            }
            AnimatedVisibility(showFilterField) {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { onFilterQueryChange(it) },
                    label = { Text("Suche (Nummer oder Thema)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (groupByTimesUsed && groupedSpeeches != null) {
                groupedSpeeches.forEach { (timesUsed, speechList) ->
                    stickyHeader {
                        SpeechGroupHeader(timesUsed.toString())
                    }
                    items(speechList, key = { it.speech.id.ifBlank { it.hashCode() } }) { speechWithUsage ->
                        SpeechListItem(
                            speechWithUsageHistory = speechWithUsage,
                            onLongClick = { onSelectSpeech(speechWithUsage.speech) }
                        )
                    }
                }
            } else {
                items(filteredSpeeches, key = { it.speech.id.ifBlank { it.hashCode() } }) { speechWithUsage ->
                    SpeechListItem(
                        speechWithUsageHistory = speechWithUsage,
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
    val speeches = listOf(
        SpeechWithUsageHistory(Speech(id = "1", number = "1", subject = "Rede 1"), 3),
        SpeechWithUsageHistory(Speech(id = "2", number = "2", subject = "Rede 2"), 1),
        SpeechWithUsageHistory(Speech(id = "3", number = "3", subject = "Ein anderes Thema"), 5)
    )
    SpeechPlaningTheme {
        SpeechListContent(
            speeches = speeches,
            filterQuery = "",
            onFilterQueryChange = {},
            showFilterField = false,
            onToggleFilterVisibility = {},
            groupByTimesUsed = false,
            onToggleGroupByTimesUsed = {},
            filteredSpeeches = speeches,
            groupedSpeeches = null,
            onSelectSpeech = {}
        )
    }
}
