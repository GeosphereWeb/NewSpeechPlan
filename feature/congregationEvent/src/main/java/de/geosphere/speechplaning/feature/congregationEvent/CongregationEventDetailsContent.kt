package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

/**
 * Stateless Composable für die Inhalte der Detail-Ansicht
 */
@Composable
fun CongregationEventDetailsContent(
    congregationEvent: CongregationEvent?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    stringProvider: AppEventStringProvider
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ereignis-Details",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (congregationEvent == null) {
                Text("Ereignis nicht gefunden")
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onBack) { Text("Zurück") }
                return@Column
            }

            val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("dd. MMMM yyyy") }
            Text("Datum: ${congregationEvent.date?.format(formatter) ?: congregationEvent.dateString}")
            Text("Typ: ${stringProvider.getStringForEvent(congregationEvent.eventType)}")
            Text("Redner: ${congregationEvent.speakerName ?: "-"}")
            Text("Versammlung: ${congregationEvent.speakerCongregationName ?: "-"}")
            Text("Ansprache: ${congregationEvent.speechNumber ?: "-"} ${congregationEvent.speechSubject ?: ""}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Notizen:")
            Text(congregationEvent.notes ?: "")

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onBack) { Text("Zurück") }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onEdit) { Text("Bearbeiten") }
            }
        }
    }
}

/**
 * Stateful Composable für die Detail-Ansicht (ursprüngliche Implementierung)
 */
@Composable
fun CongregationEventDetailsScreen(
    congregationEvent: CongregationEvent?,
    onBack: () -> Unit,
    onEdit: (CongregationEvent?) -> Unit,
    stringProvider: AppEventStringProvider
) {
    CongregationEventDetailsContent(
        congregationEvent = congregationEvent,
        onBack = onBack,
        onEdit = { onEdit(congregationEvent) },
        stringProvider = stringProvider
    )
}

@ThemePreviews
@Composable
fun CongregationEventDetailsContentPreview() = SpeechPlaningTheme {
    val mockEvent = CongregationEvent(
        id = "1",
        dateString = "2026-01-15",
        speechNumber = "123",
        speechSubject = "Vortrag über Glauben",
        speakerName = "Müller, Max",
        speakerCongregationName = "Berlin-Mitte",
        eventType = Event.MISCELLANEOUS,
        notes = "Bitte beachten: Der Redner benötigt einen Beamer für seine Präsentation."
    )
    CongregationEventDetailsContent(
        congregationEvent = mockEvent,
        onBack = {},
        onEdit = {},
        stringProvider = AppEventStringProvider(context = LocalContext.current)
    )
}

@ThemePreviews
@Composable
fun CongregationEventDetailsContentEmptyPreview() = SpeechPlaningTheme {
    CongregationEventDetailsContent(
        congregationEvent = null,
        onBack = {},
        onEdit = {},
        stringProvider = AppEventStringProvider(context = LocalContext.current)
    )
}
