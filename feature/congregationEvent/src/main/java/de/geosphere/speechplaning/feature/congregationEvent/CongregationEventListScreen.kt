package de.geosphere.speechplaning.feature.congregationEvent

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun CongregationEventListScreen(
    viewModel: CongregationEventViewModel = koinViewModel(),
    stringProvider: AppEventStringProvider = koinInject()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CongregationEventUiState.LoadingUiState -> {
            CongregationEventLoadingContent()
        }

        is CongregationEventUiState.ErrorUiState -> {
            CongregationEventErrorContent(message = state.message)
        }

        is CongregationEventUiState.SuccessUiState -> {
            CongregationEventSuccessContent(
                state = state,
                stringProvider = stringProvider,
                onNavigateToDetails = { viewModel.selectCongregationEvent(it) },
                onEventSelect = { navController, event ->
                    navController.navigate("details/${event.id}")
                },
                onEditEvent = { navController, event ->
                    viewModel.selectCongregationEvent(event)
                    navController.navigate("list")
                },
                onDismissEditDialog = viewModel::clearSelection,
                onSaveEvent = viewModel::saveCongregationEvent,
                onDeleteEvent = viewModel::deleteCongregationEvent
            )
        }
    }
}

/**
 * Stateless Composable für Loading State
 */
@Composable
fun CongregationEventLoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Stateless Composable für Error State
 */
@Composable
fun CongregationEventErrorContent(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = androidx.compose.material3.MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = androidx.compose.material3.MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = {}) {
            Text("Wiederholen")
        }
    }
}

/**
 * Stateless Composable für Success State
 */
@Composable
fun CongregationEventSuccessContent(
    state: CongregationEventUiState.SuccessUiState,
    stringProvider: AppEventStringProvider,
    onNavigateToDetails: (CongregationEvent?) -> Unit,
    onEventSelect: (androidx.navigation.NavController, CongregationEvent) -> Unit,
    onEditEvent: (androidx.navigation.NavController, CongregationEvent?) -> Unit,
    onDismissEditDialog: () -> Unit,
    onSaveEvent: (CongregationEvent) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            Scaffold(
                floatingActionButton = {
                    if (state.canCreateCongregationEvent) {
                        FloatingActionButton(onClick = { onNavigateToDetails(null) }) {
                            Icon(Icons.Default.Add, contentDescription = "Neues Ereignis")
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    CongregationEventListContent(
                        congregationEvents = state.congregationEvents,
                        onSelectCongregationEvent = { onEventSelect(navController, it) },
                        stringProvider = stringProvider,
                        isWhatsAppInstalled = state.isWhatsAppInstalled
                    )

                    if (state.isActionInProgress) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    state.actionError?.let {
                        Text(
                            text = it,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        )
                    }

                    if (state.showEditDialog) {
                        CongregationEventEditDialog(
                            congregationEvent = state.selectedCongregationEvent,
                            allSpeakers = state.allSpeakers,
                            allCongregations = state.allCongregations,
                            allSpeeches = state.allSpeeches,
                            onDismiss = onDismissEditDialog,
                            onSave = onSaveEvent,
                            onDelete = onDeleteEvent,
                            stringProvider = stringProvider
                        )
                    }
                }
            }
        }

        composable("details/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val event = state.congregationEvents.find { it.id == id }
            CongregationEventDetailsScreen(
                congregationEvent = event,
                onBack = { navController.popBackStack() },
                onEdit = { ev -> onEditEvent(navController, ev) },
                stringProvider = stringProvider
            )
        }
    }
}

private fun createMockUiState(): CongregationEventUiState.SuccessUiState {
    val mockEvents = listOf(
        CongregationEvent(
            id = "1",
            dateString = "2026-01-15",
            speechNumber = "123",
            speechSubject = "Vortrag über Glauben",
            speakerName = "Müller, Max",
            speakerCongregationName = "Berlin-Mitte",
            eventType = de.geosphere.speechplaning.core.model.data.Event.MEMORIAL
        ),
        CongregationEvent(
            id = "2",
            dateString = "2026-01-22",
            speechNumber = "456",
            speechSubject = "Hoffnung für die Zukunft",
            speakerName = "Schmidt, Lisa",
            speakerCongregationName = "Hamburg-Nord",
            eventType = de.geosphere.speechplaning.core.model.data.Event.MEMORIAL
        ),
        CongregationEvent(
            id = "3",
            dateString = "2026-02-05",
            speechNumber = "789",
            speechSubject = "Gottes Königreich",
            speakerName = "Weber, Thomas",
            speakerCongregationName = "München-Süd",
            eventType = de.geosphere.speechplaning.core.model.data.Event.CIRCUIT_ASSEMBLY
        )
    )

    return CongregationEventUiState.SuccessUiState(
        congregationEvents = mockEvents,
        canCreateCongregationEvent = true,
        isActionInProgress = false,
        actionError = null,
        showEditDialog = false,
        selectedCongregationEvent = null,
        allSpeakers = listOf(
            de.geosphere.speechplaning.core.model.Speaker(
                id = "s1", firstName = "Max", lastName = "Müller",
                speechNumberIds = listOf(1, 2, 3)
            )
        ),
        allCongregations = listOf(
            de.geosphere.speechplaning.core.model.Congregation(id = "c1", name = "Berlin-Mitte")
        ),
        allSpeeches = listOf(
            de.geosphere.speechplaning.core.model.Speech(id = "sp1", number = "123", subject = "Vortrag über Glauben")
        )
    )
}

@ThemePreviews
@Composable
fun CongregationEventLoadingContentPreview() = SpeechPlaningTheme {
    CongregationEventLoadingContent()
}

@ThemePreviews
@Composable
fun CongregationEventErrorContentPreview() = SpeechPlaningTheme {
    CongregationEventErrorContent(message = "Fehler beim Laden der Ereignisse")
}

/**
 * Preview für die Success-View ohne echte Navigation
 * Zeigt die Liste mit Mock-Daten
 */
@ThemePreviews
@Composable
fun CongregationEventSuccessListPreview() = SpeechPlaningTheme {
    val mockState = createMockUiState()

    Scaffold(
        floatingActionButton = {
            androidx.compose.material3.FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Neues Ereignis")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            CongregationEventListContent(
                congregationEvents = mockState.congregationEvents,
                onSelectCongregationEvent = {},
                stringProvider = AppEventStringProvider(context = LocalContext.current),
                isWhatsAppInstalled = true
            )
        }
    }
}
