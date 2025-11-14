package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.core.ui.atoms.SpeakerListItemComposable

@Composable
fun SpeakerListScreen(
    viewModel: SpeakerViewModel,
    onAddSpeaker: () -> Unit,
    onSpeakerClick: (Speaker) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    SpeakerListContent(
        uiState = uiState,
        onAddSpeaker = onAddSpeaker,
        onSpeakerClick = onSpeakerClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeakerListContent(
    uiState: SpeakerUiState,
    onAddSpeaker: () -> Unit,
    onSpeakerClick: (Speaker) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Speakers") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSpeaker) {
                Icon(Icons.Filled.Add, contentDescription = "Add Speaker")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = "Error: ${uiState.error}")
                }
                else -> {
                    LazyColumn {
                        items(uiState.speakers) { speaker ->
                            SpeakerListItemComposable(
                                speaker = speaker,
                                onClick = { onSpeakerClick(speaker) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Default State", showBackground = true)
@Composable
private fun SpeakerListContentPreview() {
    val speakers = listOf(
        Speaker(id = "1", nameFirst = "John", nameLast = "Doe", spiritualStatus = SpiritualStatus.ELDER),
        Speaker(
            id = "2",
            nameFirst = "Jane",
            nameLast = "Smith",
            spiritualStatus = SpiritualStatus.MINISTERIAL_SERVANT
        ),
        Speaker(id = "3", nameFirst = "Peter", nameLast = "Jones", spiritualStatus = SpiritualStatus.UNKNOWN)
    )
    val uiState = SpeakerUiState(isLoading = false, speakers = speakers)
    SpeakerListContent(uiState = uiState, onAddSpeaker = {}, onSpeakerClick = {})
}

@Preview(name = "Loading State", showBackground = true)
@Composable
private fun SpeakerListLoadingPreview() {
    val uiState = SpeakerUiState(isLoading = true)
    SpeakerListContent(uiState = uiState, onAddSpeaker = {}, onSpeakerClick = {})
}

@Preview(name = "Error State", showBackground = true)
@Composable
private fun SpeakerListErrorPreview() {
    val uiState = SpeakerUiState(isLoading = false, error = "Could not load speakers.")
    SpeakerListContent(uiState = uiState, onAddSpeaker = {}, onSpeakerClick = {})
}
