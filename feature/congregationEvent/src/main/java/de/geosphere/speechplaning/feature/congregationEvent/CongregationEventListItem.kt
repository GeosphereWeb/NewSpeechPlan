package de.geosphere.speechplaning.feature.congregationEvent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
    stringProvider: AppEventStringProvider
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

        Box(modifier = Modifier) {
            if (congregationEvent.eventType != Event.CONGREGATION) {
                Badge(
                    containerColor = if (congregationEvent.eventType == Event.MEMORIAL) {
                        colorScheme.tertiaryContainer
                    } else {
                        colorScheme.primaryContainer
                    },
                    contentColor = if (congregationEvent.eventType == Event.MEMORIAL) {
                        colorScheme.onTertiaryContainer
                    } else {
                        colorScheme.onPrimaryContainer
                    }
                ) {
                    Text(
                        text = stringProvider.getStringForEvent(congregationEvent.eventType)
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val test = (
                if (congregationEvent.speechNumber != null) {
                    if (congregationEvent.speechNumber!!.toInt() < 900) {
                        congregationEvent.speechNumber
                    } else {
                        ""
                    }
                } else {
                    "-"
                }
                ).toString()
            Text(
                modifier = Modifier.defaultMinSize(34.dp),
                text = test,
                color = MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

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
                    minLines = 2,
                    color = if (congregationEvent.speechSubject.isNullOrBlank() &&
                        congregationEvent.eventType == Event.CONGREGATION
                    ) {
                        colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
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
                        fontStyle = if (congregationEvent.speakerName == null) FontStyle.Italic else FontStyle.Normal,
                        color = if (congregationEvent.speakerName != null) {
                            colorScheme.tertiary
                        } else {
                            colorScheme.tertiary.copy(alpha = 0.3f)
                        }
                    )
                    Text(
                        modifier = Modifier,
                        text = "(${congregationEvent.speakerCongregationName ?: "Unbekannt"})",
                        style = TextStyle(
                            hyphens = Hyphens.Auto,
                            lineBreak = LineBreak.Paragraph
                        ),
                        fontStyle = if (congregationEvent.speakerName == null) FontStyle.Italic else FontStyle.Normal,
                        color = if (congregationEvent.speakerName != null) {
                            colorScheme.tertiary.copy(alpha = 0.7f)
                        } else {
                            colorScheme.tertiary.copy(alpha = 0.3f)
                        }
                    )
                }
            }

            Row(modifier = Modifier) {
                val myText = congregationEvent.date?.format(formatter) ?: ""
                val myText2 = congregationEvent.date?.format(formatter2) ?: ""
                val myTextUhr = "17:30" + " Uhr"

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
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    if (congregationEvent.speakerIsInformed) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.verified),
                            contentDescription = null,
                            modifier = Modifier
                                .size(22.dp),
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
        eventType = Event.CONGREGATION,
        speakerIsInformed = true
    )
    CongregationEventListItem(
        congregationEvent = mockEvent,
        onClick = {},
        onLongClick = null,
        stringProvider = AppEventStringProvider(LocalContext.current)
    )
}
