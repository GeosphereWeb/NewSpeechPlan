package de.geosphere.speechplaning.core.ui.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Suppress("MagicNumber")
@Composable
fun SpeechListItemComposable(
    modifier: Modifier = Modifier,
    number: String,
    subject: String,
    trailingContent: @Composable (() -> Unit) = { },
    enabled: Boolean = true,
) {
    val contentAlpha = if (enabled) 1f else 0.38f

    ListItem(
        modifier = modifier.alpha(contentAlpha),
        shadowElevation = 1.dp,
        leadingContent = {
            Text(
                text = number,
                maxLines = 1,
                textAlign = TextAlign.Right,
                modifier = Modifier.width(30.dp),
                style = MaterialTheme.typography.titleMedium
            )
        },
        headlineContent = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = subject,
                    minLines = 1,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        trailingContent = trailingContent
    )
    HorizontalDivider()
}

@Suppress("MagicNumber")
@ThemePreviews
@Composable
private fun SpeechComposablePreview() = SpeechPlaningTheme {
    Column {
        SpeechListItemComposable(number = "110", subject = "Das ist der Titel dfas dafasfasdf asdf asf asdf sfd ")
        SpeechListItemComposable(number = "120", subject = "Das ist der Titel dfas dafasfasdf asdf ")
    }
}
