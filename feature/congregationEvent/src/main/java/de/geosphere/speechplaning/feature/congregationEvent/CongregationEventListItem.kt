package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import de.geosphere.speechplaning.theme.extendedColorScheme
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CongregationEventListItem(
    congregationEvent: CongregationEvent,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    stringProvider: AppEventStringProvider
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd. MMM yy") }
    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.defaultMinSize(34.dp),
                    text = congregationEvent.speechNumber ?: "-",
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    modifier = Modifier.weight(1f),
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
                        congregationEvent.eventType == Event.CONGREGATION
                    ) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Unspecified
                    }
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = congregationEvent.date?.format(formatter) ?: "",
                    fontSize = MaterialTheme.typography.bodySmall.fontSize.value.sp,
                    color = MaterialTheme.extendedColorScheme.customColor2.color
                )
            }
        },
        supportingContent = {
            val speakerInfo = congregationEvent.speakerName ?: "Kein Redner zugewiesen"
            Text(
                modifier = Modifier.padding(start = 34.dp),
                text = "$speakerInfo (${congregationEvent.speakerCongregationName ?: "Unbekannt"})",
                style = TextStyle(
                    hyphens = Hyphens.Auto,
                    lineBreak = LineBreak.Paragraph
                ),
                fontStyle = if (congregationEvent.speakerName == null) FontStyle.Italic else FontStyle.Normal,
                color = if (congregationEvent.speakerName != null) {
                    MaterialTheme.extendedColorScheme.customColor4.color
                } else {
                    MaterialTheme.extendedColorScheme.customColor4.color.copy(alpha = 0.3f)
                }
            )
        },
        // trailingContent = { Text(congregationEvent.date?.format(formatter) ?: "") },
        overlineContent = {
            if (congregationEvent.eventType != Event.CONGREGATION) {
                Badge(
                    containerColor = if (congregationEvent.eventType == Event.MEMORIAL) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = if (congregationEvent.eventType == Event.MEMORIAL) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                ) {
                    Text(
                        text = stringProvider.getStringForEvent(congregationEvent.eventType)
                    )
                }
            }
        }
    )
}

@ThemePreviews
@Composable
fun CongregationEventListItemPreview() = SpeechPlaningTheme {
    val mockEvent = CongregationEvent(
        id = "1",
        dateString = "2026-01-15",
        speechNumber = "123",
        speechSubject = "Vortrag über Glauben",
        speakerName = "Müller, Max",
        speakerCongregationName = "Berlin-Mitte",
        eventType = Event.MEMORIAL
    )
    CongregationEventListItem(
        congregationEvent = mockEvent,
        onClick = {},
        onLongClick = null,
        stringProvider = AppEventStringProvider(LocalContext.current)
    )
}

@ThemePreviews
@Composable
fun CongregationEventListItem2Preview() = SpeechPlaningTheme {
    val mockEvent = CongregationEvent(
        id = "1",
        dateString = "2026-01-15",
        speechNumber = "123",
        speechSubject = "Vortrag über Glauben",
        speakerName = null,
        speakerCongregationName = null,
        eventType = Event.MISCELLANEOUS
    )
    CongregationEventListItem(
        congregationEvent = mockEvent,
        onClick = {},
        onLongClick = null,
        stringProvider = AppEventStringProvider(LocalContext.current)
    )
}
