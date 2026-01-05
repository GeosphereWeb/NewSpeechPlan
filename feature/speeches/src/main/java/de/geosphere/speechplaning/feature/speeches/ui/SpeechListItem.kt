package de.geosphere.speechplaning.feature.speeches.ui

import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.geosphere.speechplaning.core.model.Speech
import de.geosphere.speechplaning.core.ui.atoms.SpeechListItemComposable
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeechListItem(speech: Speech, onClick: () -> Unit, onLongClick: (() -> Unit)?) {
    SpeechListItemComposable(
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        ),
        number = speech.number,
        subject = speech.subject,
        enabled = speech.active // Hier wird der Status an die UI weitergereicht
    )
}

@ThemePreviews
@Composable
fun SpeechListItemPreview() {
    SpeechPlaningTheme {
        SpeechListItem(
            speech = Speech(
                id = "123",
                number = "142",
                subject = "Ist die Hölle ein Ort der Qualen?",
                active = true
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
            speech = Speech(
                id = "124",
                number = "99",
                subject = "Dies ist eine inaktive Rede",
                active = false
            ),
            onClick = {},
            onLongClick = {}
        )
    }
}
