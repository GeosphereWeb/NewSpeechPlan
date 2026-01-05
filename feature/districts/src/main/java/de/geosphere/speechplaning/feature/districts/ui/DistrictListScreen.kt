package de.geosphere.speechplaning.feature.districts.ui

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
import de.geosphere.speechplaning.core.model.District
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun DistrictListScreen(viewModel: DistrictsViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DistrictListScreenContent(
        uiState = uiState,
        onSelectDistrict = viewModel::selectDistrict,
        onClearSelection = viewModel::clearSelection,
        onSaveDistrict = viewModel::saveDistrict,
        onDeleteDistrict = viewModel::deleteDistrict
    )
}

@Composable
fun DistrictListScreenContent(
    uiState: DistrictUiState,
    onSelectDistrict: (District) -> Unit,
    onClearSelection: () -> Unit,
    onSaveDistrict: (District) -> Unit,
    onDeleteDistrict: (String) -> Unit
) {
    when (uiState) {
        is DistrictUiState.LoadingUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DistrictUiState.ErrorUIState -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Error")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        is DistrictUiState.SuccessUIState -> {
            Scaffold(
                floatingActionButton = {
                    if (uiState.canEditDistrict) {
                        FloatingActionButton(onClick = { onSelectDistrict(District()) }) {
                            Icon(Icons.Default.Add, contentDescription = "Add District")
                        }
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                    DistrictListContent(
                        districts = uiState.districts,
                        onSelectDistrict = onSelectDistrict
                    )
                    if (uiState.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    uiState.actionError?.let { errorMsg ->
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                        )
                    }
                    uiState.selectedDistrict?.let { district ->
                        DistrictEditDialog(
                            district = district,
                            onDismiss = onClearSelection,
                            onSave = onSaveDistrict,
                            onDelete = onDeleteDistrict
                        )
                    }
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun DistrictListScreen_Success_Preview() = SpeechPlaningTheme {
    DistrictListScreenContent(
        uiState = DistrictUiState.SuccessUIState(
            districts = listOf(
                District(id = "1", name = "District 1"),
                District(id = "2", name = "District 2")
            ),
            canEditDistrict = true
        ),
        onSelectDistrict = {},
        onClearSelection = {},
        onSaveDistrict = {},
        onDeleteDistrict = {}
    )
}
