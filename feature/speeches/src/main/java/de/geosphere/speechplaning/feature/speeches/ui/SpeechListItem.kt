package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.model.SpeechWithUsageCount
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews
import de.geosphere.speechplaning.theme.extendedColorScheme

@Composable
fun SpeechListItem(speechWithUsage: SpeechWithUsageCount, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    val contentAlpha = if (speechWithUsage.speech.active) 1f else 0.38f
    ListItem(
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .alpha(contentAlpha),
        shadowElevation = 1.dp,
        leadingContent = {
            Text(
                modifier = Modifier.defaultMinSize(34.dp),
                text = speechWithUsage.speech.number,
                maxLines = 1,
                textAlign = TextAlign.Right,
                color = MaterialTheme.extendedColorScheme.customColor4.color
            )
        },
        headlineContent = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = speechWithUsage.speech.subject,
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
                text = "${speechWithUsage.timesUsed}x",
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    )
    HorizontalDivider()
}

@ThemePreviews
@Composable
fun SpeechListItemPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speechWithUsage = SpeechWithUsageCount(
                speech = Speech(
                    id = "123",
                    number = "142",
                    subject = "Ist die Hölle ein Ort der Qualen?",
                    active = true
                ),
                timesUsed = 5
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}

@ThemePreviews
@Composable
fun SpeechListItemDisabledPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speechWithUsage = SpeechWithUsageCount(
                speech = Speech(
                    id = "124",
                    number = "99",
                    subject = "Dies ist eine inaktive Rede",
                    active = false
                ),
                timesUsed = 2
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}
