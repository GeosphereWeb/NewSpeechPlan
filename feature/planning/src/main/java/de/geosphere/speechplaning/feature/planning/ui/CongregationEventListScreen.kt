package de.geosphere.speechplaning.feature.planning.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.atoms.di.PreviewKoin
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import java.time.LocalDate

@Composable
fun CongregationEventListScreen(
    viewModel: CongregationEventViewModel,
    districtId: String,
    congregationId: String,
    onAddEvent: () -> Unit,
    onEditEvent: (CongregationEvent) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = districtId, key2 = congregationId) {
        viewModel.loadEvents(districtId, congregationId)
    }

    CongregationEventListContent(
        uiState = uiState,
        onAddEvent = onAddEvent,
        onEditEvent = onEditEvent
    )
}

@Composable
fun CongregationEventListContent(
    uiState: CongregationEventUiState,
    onAddEvent: () -> Unit,
    onEditEvent: (CongregationEvent) -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEvent) {
                Icon(Icons.Filled.Add, contentDescription = "Add Event")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.events) { event ->
                    ListItem(
                        headlineContent = { Text(event.eventType.name) },
                        supportingContent = { Text(event.date.toString()) },
                        modifier = Modifier.clickable { onEditEvent(event) }
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun CongregationEventListScreenPreviewWithData() = PreviewKoin {
    SpeechPlaningTheme {
        val sampleEvents = listOf(
            CongregationEvent(
                id = "1",
                congregationId = "c1",
                date = LocalDate.now(),
                eventType = Event.CONVENTION,
                speechId = "s1",
                speakerId = "p1"
            ),
            CongregationEvent(
                id = "2",
                congregationId = "c1",
                date = LocalDate.now().plusDays(7),
                eventType = Event.CONVENTION
            )
        )
        val uiState = CongregationEventUiState(events = sampleEvents)
        CongregationEventListContent(
            uiState = uiState,
            onAddEvent = {},
            onEditEvent = {}
        )
    }
}

@ThemePreviews
@Composable
@Suppress("kotlin:S100")
fun CongregationEventListScreenPreview_Loading() = PreviewKoin {
    SpeechPlaningTheme {
        val uiState = CongregationEventUiState(isLoading = true)
        CongregationEventListContent(
            uiState = uiState,
            onAddEvent = {},
            onEditEvent = {}
        )
    }
}

@ThemePreviews
@Composable
@Suppress("kotlin:S100")
fun CongregationEventListScreenPreview_Error() = PreviewKoin {
    SpeechPlaningTheme {
        val uiState = CongregationEventUiState(error = "Could not load events.")
        CongregationEventListContent(
            uiState = uiState,
            onAddEvent = {},
            onEditEvent = {}
        )
    }
}

@ThemePreviews
@Composable
@Suppress("kotlin:S100")
fun CongregationEventListScreenPreview_Empty() = PreviewKoin {
    SpeechPlaningTheme {
        val uiState = CongregationEventUiState(events = emptyList())
        CongregationEventListContent(
            uiState = uiState,
            onAddEvent = {},
            onEditEvent = {}
        )
    }
}
