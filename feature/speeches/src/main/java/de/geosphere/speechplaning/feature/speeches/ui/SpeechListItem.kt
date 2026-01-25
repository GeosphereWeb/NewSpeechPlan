package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.data.SpeechWithUsageHistory
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import de.geosphere.speechplaning.theme.extendedColorScheme

@Composable
fun SpeechListItem(speechWithUsageHistory: SpeechWithUsageHistory, onLongClick: (() -> Unit)?) {
    val contentAlpha = if (speechWithUsageHistory.speech.active) 1f else 0.38f
    var toggleZusatzinfo by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    toggleZusatzinfo = !toggleZusatzinfo
                },
                onLongClick = onLongClick
            )
            .alpha(contentAlpha),
        shadowElevation = 1.dp,
        leadingContent = {
            Text(
                modifier = Modifier.defaultMinSize(34.dp),
                text = speechWithUsageHistory.speech.number,
                maxLines = 1,
                textAlign = TextAlign.Right,
                color = MaterialTheme.extendedColorScheme.customColor4.color
            )
        },
        headlineContent = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Titel
                Text(
                    text = speechWithUsageHistory.speech.subject,
                    minLines = 1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        hyphens = Hyphens.Auto,
                        lineBreak = LineBreak(
                            strategy = LineBreak.Strategy.HighQuality,
                            strictness = LineBreak.Strictness.Normal,
                            wordBreak = LineBreak.WordBreak.Default
                        )
                    ),
                )
            }
        },
        trailingContent = {
            Text(
                text = "${speechWithUsageHistory.timesUsed}x",
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        },
        supportingContent = {
            // Letzte Verwendungen anzeigen
            AnimatedVisibility(toggleZusatzinfo && speechWithUsageHistory.usageHistory.isNotEmpty()) {
                Column {
                    Text(
                        text = "Letzte Verwendungen:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Zeige maximal 2 letzte Verwendungen
                    speechWithUsageHistory.usageHistory.forEach { usage ->
                        Text(
                            text = "• ${usage.dateString} - ${usage.speakerName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            // if (toggleZusatzinfo && speechWithUsageHistory.usageHistory.isNotEmpty()) {
            // }
        }
    )
    HorizontalDivider()
}

@ThemePreviews
@Composable
fun SpeechListItemPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speechWithUsageHistory = SpeechWithUsageHistory(
                speech = Speech(
                    id = "123",
                    number = "142",
                    subject = "Ist die Hölle ein Ort der Qualen?",
                    active = true
                ),
                timesUsed = 3,
                usageHistory = listOf(
                    de.geosphere.speechplaning.core.model.data.SpeechUsageDetail(
                        dateString = "22.01.2026",
                        speakerName = "Max Müller"
                    ),
                    de.geosphere.speechplaning.core.model.data.SpeechUsageDetail(
                        dateString = "15.01.2026",
                        speakerName = "Anna Schmidt"
                    ),
                    de.geosphere.speechplaning.core.model.data.SpeechUsageDetail(
                        dateString = "15.01.2026",
                        speakerName = "Anna Schmidt"
                    )

                )
            ),
            onLongClick = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeechListItemDisabledPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speechWithUsageHistory = SpeechWithUsageHistory(
                speech = Speech(
                    id = "124",
                    number = "99",
                    subject = "Dies ist eine inaktive Rede",
                    active = false
                ),
                timesUsed = 2
            ),
            onLongClick = {}
        )
    }
}
