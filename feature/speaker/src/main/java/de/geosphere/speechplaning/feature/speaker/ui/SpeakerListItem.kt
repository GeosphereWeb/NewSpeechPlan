package de.geosphere.speechplaning.feature.speaker.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.ui.atoms.AppAvatarResourceProvider
import de.geosphere.speechplaning.core.ui.atoms.AvatarProvider
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun SpeakerListItem(
    modifier: Modifier,
    speaker: Speaker,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier.then(
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(
                    id = AvatarProvider(AppAvatarResourceProvider()).getAvatar(
                        speaker.spiritualStatus
                    )
                ),
                contentDescription = "Avatar für ${speaker.spiritualStatus}",
                tint = Color.Unspecified
            )
            Column(modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)) {
                Text(
                    text = "${speaker.firstName} ${speaker.lastName}",
                    style = MaterialTheme.typography.titleMedium
                )
                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        if (speaker.mobile.isNotBlank()) {
                            Text(
                                text = "Mobil: ${speaker.mobile}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (speaker.phone.isNotBlank()) {
                            Text(
                                text = "Telefon: ${speaker.phone}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (speaker.email.isNotBlank()) {
                            Text(
                                text = "E-Mail: ${speaker.email}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "Status: ${speaker.spiritualStatus.name}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            }
            val context = LocalContext.current
            val hasPhoneNumber = speaker.mobile.isNotBlank()

            if (isExpanded && hasPhoneNumber) {
                val numberToDail = speaker.mobile.ifBlank { speaker.phone }
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numberToDail"))

                IconButton(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = {
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF19812A),
                        imageVector = Icons.Default.Phone, contentDescription = null
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun SpeakerListItemPreview() {
    SpeechPlaningTheme {
        SpeakerListItem(
            modifier = Modifier,
            speaker = Speaker(firstName = "Max", lastName = "Mustermann"),
            isExpanded = false,
            onClick = {}
        ) {}
    }
}

@ThemePreviews
@Composable
private fun SpeakerListItemExpandedPreview() {
    SpeechPlaningTheme {
        SpeakerListItem(
            modifier = Modifier,
            speaker = Speaker(firstName = "Erika", lastName = "Mustermann"),
            isExpanded = true,
            onClick = {}
        ) {}
    }
}
