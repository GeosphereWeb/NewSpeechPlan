package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.ThemePreviews
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CongregationEventListItem(
    congregationEvent: CongregationEvent,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    stringProvider: AppEventStringProvider
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd. MMMM yyyy") }
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = {
            Row {
                Text(
                    modifier = Modifier.defaultMinSize(34.dp),
                    text = congregationEvent.speechNumber ?: "-"
                )
                Text(
                    text = congregationEvent.speechSubject ?: "Ereignis ohne Thema",
                    style = TextStyle(
                        hyphens = Hyphens.Auto,
                        lineBreak = LineBreak(
                            strategy = LineBreak.Strategy.HighQuality,
                            strictness = LineBreak.Strictness.Normal,
                            wordBreak = LineBreak.WordBreak.Default
                        )
                    ),
                    color = if (congregationEvent.speechSubject.isNullOrBlank() &&
                        congregationEvent.eventType == Event.MISCELLANEOUS
                    )
                        Color.Red else Color.Unspecified
                )
            }
        },
        supportingContent = {
            val speakerInfo = congregationEvent.speakerName ?: "Kein Redner zugewiesen"
            Text(
                text = "$speakerInfo (${congregationEvent.speakerCongregationName ?: "Unbekannt"})",
                style = TextStyle(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph
                )
            )
        },
        trailingContent = { Text(congregationEvent.date?.format(formatter) ?: "") },
        overlineContent = {
            Text(
                text = if (congregationEvent.eventType != Event.MISCELLANEOUS) {
                    stringProvider.getStringForEvent(congregationEvent.eventType)
                } else {
                    ""
                }
            )
        }
    )
}


@ThemePreviews
@Composable
fun CongregationEventListItemPreview() {
    val mockEvent = CongregationEvent(
        id = "1",
        dateString = "2026-01-15",
        speechNumber = "123",
        speechSubject = "Vortrag über Glauben",
        speakerName = "Müller, Max",
        speakerCongregationName = "Berlin-Mitte",
        eventType = Event.MISCELLANEOUS
    )
    CongregationEventListItem(
        congregationEvent = mockEvent,
        onClick = {},
        onLongClick = null,
        stringProvider = AppEventStringProvider(LocalContext.current)
    )
}
