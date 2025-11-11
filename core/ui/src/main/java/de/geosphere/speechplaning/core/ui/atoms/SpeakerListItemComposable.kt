package de.geosphere.speechplaning.core.ui.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speaker

@Composable
fun SpeakerListItemComposable(
    speaker: Speaker,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            Column {
                Text(
                    text = "${speaker.nameFirst} ${speaker.nameLast}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = speaker.spiritualStatus.name,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
