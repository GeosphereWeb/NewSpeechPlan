package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.runtime.Composable
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.ui.atoms.SpeakerListItemComposable
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeakerListItem(
    speaker: Speaker,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    SpeakerListItemComposable(
        speaker = speaker,
        isExpanded = isExpanded,
        onClick = onClick,
        onLongClick = onLongClick
    )
}

@ThemePreviews
@Composable
private fun SpeakerListItemPreview() {
    SpeechPlaningTheme {
        SpeakerListItem(
            speaker = Speaker(firstName = "Max", lastName = "Mustermann"),
            isExpanded = false,
            onClick = {},
            onLongClick = {}
        )
    }
}

@ThemePreviews
@Composable
private fun SpeakerListItemExpandedPreview() {
    SpeechPlaningTheme {
        SpeakerListItem(
            speaker = Speaker(firstName = "Erika", lastName = "Mustermann"),
            isExpanded = true,
            onClick = {},
            onLongClick = {}
        )
    }
}
