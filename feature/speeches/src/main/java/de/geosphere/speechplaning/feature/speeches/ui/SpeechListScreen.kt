package de.geosphere.speechplaning.feature.speeches.ui

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
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpeechListScreen(viewModel: SpeechViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpeechListScreenContent(
        uiState = uiState,
        onSelectSpeech = viewModel::selectSpeech,
        onClearSelection = viewModel::clearSelection,
        onSaveSpeech = viewModel::saveSpeech,
        onDeleteSpeech = viewModel::deleteSpeech
    )
}

@Composable
fun SpeechListScreenContent(
    uiState: SpeechUiState,
    onSelectSpeech: (Speech) -> Unit,
    onClearSelection: () -> Unit,
    onSaveSpeech: (Speech) -> Unit,
    onDeleteSpeech: (String) -> Unit
) {
    when (val state = uiState) {
        is SpeechUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is SpeechUiState.ErrorUIState -> {
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

        is SpeechUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (state.canEditSpeech) {
                        FloatingActionButton(onClick = { onSelectSpeech(Speech()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Speech")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    SpeechListContent(
                        speeches = state.speeches,
                        onSelectSpeech = onSelectSpeech
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

                    state.selectedSpeech?.let { speech ->
                        SpeechEditDialog(
                            speech = speech,
                            onDismiss = onClearSelection,
                            onSave = onSaveSpeech,
                            onDelete = onDeleteSpeech
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeechListScreen_Success_Preview() {
    SpeechPlaningTheme {
        SpeechListScreenContent(
            uiState = SpeechUiState.SuccessUIState(
                speeches = listOf(
                    Speech(id = "1", number = "1", subject = "Rede 1"),
                    Speech(id = "2", number = "2", subject = "Rede 2"),
                    Speech(id = "3", number = "3", subject = "Ein anderes Thema")
                ),
                canEditSpeech = true
            ),
            onSelectSpeech = {},
            onClearSelection = {},
            onSaveSpeech = {},
            onDeleteSpeech = {}
        )
    }
}
