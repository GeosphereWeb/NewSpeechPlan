package de.geosphere.speechplaning.feature.planning.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.atoms.di.PreviewKoin
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import java.time.LocalDate

@Composable
fun CongregationEventEditorScreen(
    viewModel: CongregationEventViewModel,
    districtId: String,
    congregationId: String,
    event: CongregationEvent?,
    onSave: () -> Unit
) {
    var eventType by remember { mutableStateOf(event?.eventType ?: Event.CONVENTION) }
    var date by remember { mutableStateOf(event?.date?.toString() ?: LocalDate.now().toString()) }

    LaunchedEffect(event) {
        event?.let {
            eventType = it.eventType
            date = it.date.toString()
        }
    }

    CongregationEventEditorContent(
        eventType = eventType,
        date = date,
        onDateChange = { date = it },
        onSave = {
            val updatedEvent = (event ?: CongregationEvent(date = LocalDate.now(), eventType = Event.CONVENTION)).copy(
                eventType = eventType,
                date = LocalDate.parse(date)
            )
            viewModel.saveEvent(districtId, congregationId, updatedEvent)
            onSave()
        }
    )
}

@Composable
fun CongregationEventEditorContent(
    eventType: Event,
    date: String,
    onDateChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            // Add more fields as needed
            OutlinedTextField(
                value = eventType.name,
                onValueChange = { /* Implement a way to select event type */ },
                label = { Text("Event Type") },
                readOnly = true // Example, replace with a dropdown
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("Date") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("Date") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onSave) {
                Text("Save")
            }
        }
    }
}

@ThemePreviews
@Composable
@Suppress("kotlin:S100")
fun CongregationEventEditorScreenPreview_New() = PreviewKoin {
    SpeechPlaningTheme {
        CongregationEventEditorContent(
            eventType = Event.CONVENTION,
            date = LocalDate.now().toString(),
            onDateChange = {},
            onSave = {}
        )
    }
}

@ThemePreviews
@Composable
@Suppress("kotlin:S100")
fun CongregationEventEditorScreenPreview_Edit() = PreviewKoin {
    SpeechPlaningTheme {
        CongregationEventEditorContent(
            eventType = Event.CONVENTION,
            date = LocalDate.now().plusWeeks(1).toString(),
            onDateChange = {},
            onSave = {}
        )
    }
}
