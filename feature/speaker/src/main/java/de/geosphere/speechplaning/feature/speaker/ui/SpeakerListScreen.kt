package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.geosphere.speechplaning.core.model.Congregation
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeakerListScreen(viewModel: SpeakerViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SpeakerListScreenContent(
        uiState = uiState,
        onSelectSpeaker = viewModel::selectSpeaker,
        onClearSelection = viewModel::clearSelection,
        onSaveSpeaker = viewModel::saveSpeaker,
        onDeleteSpeaker = viewModel::deleteSpeaker
    )
}

@Composable
fun SpeakerListScreenContent(
    uiState: SpeakerUiState,
    onSelectSpeaker: (Speaker) -> Unit,
    onClearSelection: () -> Unit,
    onSaveSpeaker: (Speaker) -> Unit,
    onDeleteSpeaker: (String) -> Unit
) {
    when (uiState) {
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
                    Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        is SpeakerUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (uiState.canEditSpeaker) {
                        FloatingActionButton(onClick = { onSelectSpeaker(Speaker()) }) {
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
                        speakers = uiState.speakers,
                        allCongregations = uiState.allCongregations,
                        onSelectSpeaker = onSelectSpeaker
                    )

                    if (uiState.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    uiState.actionError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }

                    uiState.selectedSpeaker?.let { speaker ->
                        SpeakerEditDialog(
                            speaker = speaker,
                            allCongregations = uiState.allCongregations,
                            allSpeeches = uiState.allSpeeches,
                            onDismiss = onClearSelection,
                            onSave = onSaveSpeaker,
                            onDelete = onDeleteSpeaker
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeakerListScreen_Success_Preview() {
    SpeechPlaningTheme {
        SpeakerListScreenContent(
            uiState = SpeakerUiState.SuccessUIState(
                speakers = listOf(
                    Speaker(id = "s1", firstName = "Max", lastName = "Mustermann", congregationId = "c1"),
                    Speaker(id = "s2", firstName = "Erika", lastName = "Mustermann", congregationId = "c1"),
                    Speaker(id = "s3", firstName = "John", lastName = "Doe", congregationId = "c2"),
                ),
                allCongregations = listOf(
                    Congregation(id = "c1", name = "Musterstadt"),
                    Congregation(id = "c2", name = "Beispielburg")
                ),
                allSpeeches = listOf(
                    Speech(id = "1", number = "1", subject = "Wie gut kennst du Gott?")
                ),
                canEditSpeaker = true
            ),
            onSelectSpeaker = {},
            onClearSelection = {},
            onSaveSpeaker = {},
            onDeleteSpeaker = {}
        )
    }
}
