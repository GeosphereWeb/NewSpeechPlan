package de.geosphere.speechplaning.feature.congregation

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
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun CongregationListScreen(viewModel: CongregationViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CongregationListScreenContent(
        uiState = uiState,
        onSelectCongregation = viewModel::selectCongregation,
        onClearSelection = viewModel::clearSelection,
        onSaveCongregation = viewModel::saveCongregation,
        onDeleteCongregation = viewModel::deleteCongregation,
    )
}

@Composable
fun CongregationListScreenContent(
    uiState: CongregationUiState,
    onSelectCongregation: (Congregation) -> Unit,
    onClearSelection: () -> Unit,
    onSaveCongregation: (Congregation) -> Unit,
    onDeleteCongregation: (String) -> Unit,
) {
    when (uiState) {
        is CongregationUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is CongregationUiState.ErrorUIState -> {
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

        is CongregationUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (uiState.canCreateCongregation) {
                        FloatingActionButton(onClick = { onSelectCongregation(Congregation()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Congregation")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    CongregationListContent(
                        congregations = uiState.congregations,
                        allDistricts = uiState.allDistricts,
                        onSelectCongregation = onSelectCongregation
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

                    uiState.selectedCongregation?.let { congregation ->
                        CongregationEditDialog(
                            congregation = congregation,
                            allDistricts = uiState.allDistricts,
                            onDismiss = onClearSelection,
                            onSave = onSaveCongregation,
                            onDelete = onDeleteCongregation
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun CongregationListScreenPreview_Success() {
    SpeechPlaningTheme {
        CongregationListScreenContent(
            uiState = CongregationUiState.SuccessUIState(
                congregations = listOf(
                    Congregation(id = "1", name = "Congregation A", districtId = "1"),
                    Congregation(id = "2", name = "Congregation B", districtId = "2"),
                    Congregation(id = "3", name = "Congregation C", districtId = "1"),
                ),
                allDistricts = listOf(
                    District(id = "1", name = "District 1"),
                    District(id = "2", name = "District 2"),
                ),
                canCreateCongregation = true
            ),
            onSelectCongregation = {},
            onClearSelection = {},
            onSaveCongregation = {},
            onDeleteCongregation = {},
        )
    }
}
