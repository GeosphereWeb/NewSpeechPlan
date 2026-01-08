package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeakerListContent(
    speakers: List<Speaker>,
    allCongregations: List<Congregation>,
    onSelectSpeaker: (Speaker) -> Unit
) {
    var congregationFilter by remember { mutableStateOf("") }
    var speakerNameFilter by remember { mutableStateOf("") }
    var expandedSpeakerId by remember { mutableStateOf<String?>(null) }

    val filteredSpeakers = remember(speakers, allCongregations, congregationFilter, speakerNameFilter) {
        speakers.filter {
            val matchesName = if (speakerNameFilter.isBlank()) {
                true
            } else {
                val searchTerms = speakerNameFilter.trim().split("\\s+".toRegex())
                val speakerFullName = "${it.firstName} ${it.lastName}"
                searchTerms.all { term -> speakerFullName.contains(term, ignoreCase = true) }
            }
            val matchesCongregation = if (congregationFilter.isBlank()) {
                true
            } else {
                val congName = allCongregations.find { c -> c.id == it.congregationId }?.name ?: ""
                congName.contains(congregationFilter, ignoreCase = true)
            }
            matchesName && matchesCongregation
        }
    }

    val groupedSpeakers = remember(filteredSpeakers, allCongregations) {
        filteredSpeakers.groupBy {
            allCongregations.find { c -> c.id == it.congregationId }?.name ?: "Unbekannt"
        }.toSortedMap()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SpeakerListFilter(
            congregationFilter = congregationFilter,
            onCongregationFilterChange = { congregationFilter = it },
            speakerNameFilter = speakerNameFilter,
            onSpeakerNameFilterChange = { speakerNameFilter = it }
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (groupedSpeakers.isEmpty()) {
                item {
                    Text(
                        text = "Keine Redner gefunden",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                groupedSpeakers.forEach { (congregationName, speakersInGroup) ->
                    stickyHeader {
                        SpeakerGroupHeader(title = congregationName)
                    }
                    items(speakersInGroup, key = { it.id }) {
                        SpeakerListItem(
                            modifier = Modifier,
                            speaker = it,
                            isExpanded = it.id == expandedSpeakerId,
                            onClick = { expandedSpeakerId = if (it.id == expandedSpeakerId) null else it.id },
                        ) { onSelectSpeaker(it) }
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeakerListContentPreview() {
    val allCongregations = listOf(
        Congregation(id = "c1", name = "Musterstadt"),
        Congregation(id = "c2", name = "Beispielburg")
    )
    val speakers = listOf(
        Speaker(id = "s1", firstName = "Max", lastName = "Mustermann", congregationId = "c1"),
        Speaker(id = "s2", firstName = "Erika", lastName = "Mustermann", congregationId = "c1"),
        Speaker(id = "s3", firstName = "John", lastName = "Doe", congregationId = "c2"),
        Speaker(id = "s4", firstName = "Jane", lastName = "Doe", congregationId = "")
    )
    SpeechPlaningTheme {
        SpeakerListContent(
            speakers = speakers,
            allCongregations = allCongregations,
            onSelectSpeaker = {}
        )
    }
}
