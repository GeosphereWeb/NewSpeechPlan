@file:Suppress("TooManyFunctions")

package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.geosphere.speechplaning.core.model.CongregationEvent
import de.geosphere.speechplaning.core.model.data.Event
import de.geosphere.speechplaning.core.ui.provider.AppEventIconProvider
import de.geosphere.speechplaning.core.ui.provider.AppEventStringProvider
import de.geosphere.speechplaning.data.util.isInCurrentWeek
import de.geosphere.speechplaning.theme.R
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
    stringProvider: AppEventStringProvider,
    iconProvider: AppEventIconProvider
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd. MMM yy") }
    val formatter2 = remember { DateTimeFormatter.ofPattern("EEEE") }

    val isSameKW = congregationEvent.date?.isInCurrentWeek() ?: false

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(6.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        EventBadge(congregationEvent, stringProvider, iconProvider)
        Row(verticalAlignment = Alignment.CenterVertically) {
            SpeechNumberText(congregationEvent)
            SpeechAndSpeakerInfo(congregationEvent)
            DateAndIcons(congregationEvent, formatter, formatter2, isSameKW)
        }
    }
}

@Composable
private fun EventBadge(
    congregationEvent: CongregationEvent,
    stringProvider: AppEventStringProvider,
    iconProvider: AppEventIconProvider
) {
    if (congregationEvent.eventType != Event.CONGREGATION) {
        val (containerColor, contentColor) = getBadgeColors(congregationEvent.eventType)
        val iconresource = iconProvider.getIconForEvent(congregationEvent.eventType)
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Badge(
                containerColor = containerColor,
                contentColor = contentColor) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    if(iconresource != -1) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = iconresource),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(text = stringProvider.getStringForEvent(congregationEvent.eventType))
                }

            }
        }
    }
}

@Composable
private fun getBadgeColors(eventType: Event): Pair<Color, Color> {
    return if (eventType == Event.MEMORIAL) {
        colorScheme.tertiaryContainer to colorScheme.onTertiaryContainer
    } else {
        colorScheme.primaryContainer to colorScheme.onPrimaryContainer
    }
}

@Composable
private fun RowScope.SpeechNumberText(congregationEvent: CongregationEvent) {
    val speechNumberText = getSpeechNumberText(congregationEvent)
    Text(
        modifier = Modifier.defaultMinSize(34.dp),
        text = speechNumberText,
        color = colorScheme.primary
    )
}

private const val VISIBLE_SPEECH_NUMBERS = 900

private fun getSpeechNumberText(congregationEvent: CongregationEvent): String {
    return if (congregationEvent.speechNumber != null) {
        if (congregationEvent.speechNumber!!.toInt() < VISIBLE_SPEECH_NUMBERS) {
            congregationEvent.speechNumber!!
        } else {
            ""
        }
    } else {
        "-"
    }
}

@Composable
private fun RowScope.SpeechAndSpeakerInfo(congregationEvent: CongregationEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        SpeechSubjectText(congregationEvent)
        SpeakerInfoRow(congregationEvent)
    }
}

@Composable
private fun ColumnScope.SpeechSubjectText(congregationEvent: CongregationEvent) {
    val textColor = getSpeechSubjectColor(congregationEvent)
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = congregationEvent.speechSubject ?: "Ereignis ohne Thema",
        style = TextStyle(
            hyphens = Hyphens.Auto,
            lineBreak = LineBreak(
                strategy = LineBreak.Strategy.HighQuality,
                strictness = LineBreak.Strictness.Normal,
                wordBreak = LineBreak.WordBreak.Default
            )
        ),
        color = textColor
    )
}

@Composable
private fun getSpeechSubjectColor(congregationEvent: CongregationEvent): Color {
    return if (congregationEvent.speechSubject.isNullOrBlank() && congregationEvent.eventType == Event.CONGREGATION) {
        colorScheme.error
    } else {
        colorScheme.primary
    }
}

@Composable
private fun ColumnScope.SpeakerInfoRow(congregationEvent: CongregationEvent) {
    val (speakerColor, congregationColor) = getSpeakerColors(congregationEvent)
    val fontStyle = if (congregationEvent.speakerName == null) FontStyle.Italic else FontStyle.Normal

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            modifier = Modifier.padding(end = 8.dp),
            text = congregationEvent.speakerName ?: "Kein Redner zugewiesen",
            style = TextStyle(
                hyphens = Hyphens.Auto,
                lineBreak = LineBreak.Paragraph
            ),
            fontStyle = fontStyle,
            color = speakerColor
        )
        Text(
            modifier = Modifier,
            text = "(${congregationEvent.speakerCongregationName ?: "Unbekannt"})",
            style = TextStyle(
                hyphens = Hyphens.Auto,
                lineBreak = LineBreak.Paragraph
            ),
            fontStyle = fontStyle,
            color = congregationColor
        )
    }
}

@Composable
private fun getSpeakerColors(congregationEvent: CongregationEvent): Pair<Color, Color> {
    val baseColor = if (congregationEvent.speakerName != null) {
        colorScheme.tertiary
    } else {
        colorScheme.tertiary.copy(alpha = 0.3f)
    }
    val congregationColor = if (congregationEvent.speakerName != null) {
        colorScheme.tertiary.copy(alpha = 0.7f)
    } else {
        colorScheme.tertiary.copy(alpha = 0.3f)
    }
    return baseColor to congregationColor
}

@Composable
private fun DateAndIcons(
    congregationEvent: CongregationEvent,
    formatter: DateTimeFormatter,
    formatter2: DateTimeFormatter,
    isSameKW: Boolean
) {
    Row(modifier = Modifier) {
        DateText(congregationEvent, formatter, formatter2)
        IconsColumn(congregationEvent, isSameKW)
    }
}

@Composable
private fun DateText(
    congregationEvent: CongregationEvent,
    formatter: DateTimeFormatter,
    formatter2: DateTimeFormatter
) {
    val myText = congregationEvent.date?.format(formatter) ?: ""
    val myText2 = congregationEvent.date?.format(formatter2) ?: ""
    val myTextUhr = "17:30 Uhr"

    Column {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = "$myText\n$myText2\n$myTextUhr",
            fontSize = MaterialTheme.typography.bodySmall.fontSize.value.sp,
            color = MaterialTheme.extendedColorScheme.customColor4.color,
            textAlign = TextAlign.End,
            style = TextStyle(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}

@Composable
private fun IconsColumn(congregationEvent: CongregationEvent, isSameKW: Boolean) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        if (congregationEvent.speakerIsInformed) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.verified),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.extendedColorScheme.gruen.color
            )
        }
        if (isSameKW) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.today),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.extendedColorScheme.customColor4.color.copy(alpha = 0.7f)
            )
        }
    }
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
        stringProvider = AppEventStringProvider(LocalContext.current),
        iconProvider = AppEventIconProvider(LocalContext.current),
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
        eventType = Event.CONGREGATION,
        speakerIsInformed = true
    )
    CongregationEventListItem(
        congregationEvent = mockEvent,
        onClick = {},
        onLongClick = null,
        stringProvider = AppEventStringProvider(LocalContext.current),
        iconProvider = AppEventIconProvider(LocalContext.current),
    )
}
