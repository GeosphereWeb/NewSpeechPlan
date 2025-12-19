package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.ui.atoms.SpeakerListItemComposable
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeakerListScreen(viewModel: SpeakerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is SpeakerUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SpeakerUiState.ErrorUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        is SpeakerUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (state.canEditSpeaker) {
                        FloatingActionButton(onClick = { viewModel.selectSpeaker(Speaker()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Speaker")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    SpeakerListContent(
                        speakers = state.speakers,
                        allCongregations = state.allCongregations,
                        onSelectSpeaker = viewModel::selectSpeaker
                    )

                    if (state.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    state.actionError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }

                    state.selectedSpeaker?.let { speaker ->
                        SpeakerEditDialog(
                            speaker = speaker,
                            allCongregations = state.allCongregations,
                            allSpeeches = state.allSpeeches,
                            onDismiss = viewModel::clearSelection,
                            onSave = viewModel::saveSpeaker,
                            onDelete = viewModel::deleteSpeaker
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakerListContent(
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
                            speaker = it,
                            isExpanded = it.id == expandedSpeakerId,
                            onClick = { expandedSpeakerId = if (it.id == expandedSpeakerId) null else it.id },
                            onLongClick = { onSelectSpeaker(it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakerListFilter(
    congregationFilter: String,
    onCongregationFilterChange: (String) -> Unit,
    speakerNameFilter: String,
    onSpeakerNameFilterChange: (String) -> Unit,
) {
    var filtersVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Button(onClick = { filtersVisible = !filtersVisible }) {
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

@Composable
private fun SpeakerListItem(
    speaker: Speaker,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    SpeakerListItemComposable(
        speaker = speaker,
        isExpanded = isExpanded,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@Composable
private fun SpeakerGroupHeader(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
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
