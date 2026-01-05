package de.geosphere.speechplaning.core.ui.atoms

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.data.SpiritualStatus
import de.geosphere.speechplaning.theme.SpeechPlaningTheme

@Composable
fun SpeakerListItemComposable(
    speaker: Speaker,
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
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
            modifier = Modifier.padding(16.dp).fillMaxWidth()
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
            Column(modifier = Modifier.padding(start = 16.dp).weight(1f)) {
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
                        imageVector = Icons.Default.Phone, contentDescription = null)
                }
            }
        }
    }
}

@Preview
@Composable
private fun SpeakerListItemComposableClosedPreview() {
    var isExpanded by remember { mutableStateOf(false) }
    SpeechPlaningTheme {
        SpeakerListItemComposable(
            speaker = Speaker(
                districtId = "1234567",
                firstName = "Max",
                lastName = "Mustermann",
                mobile = "+49123456789",
                phone = "+497899888874",
                email = "max.mustermann@mail.com",
                spiritualStatus = SpiritualStatus.UNKNOWN,
                congregationId = "9876543",
                active = true,
            ),
            isExpanded = isExpanded,
            onClick = { isExpanded = !isExpanded }
        )
    }
}

@Preview
@Composable
private fun SpeakerListItemComposableOpenedPreview() {
    var isExpanded by remember { mutableStateOf(true) }
    SpeechPlaningTheme {
        SpeakerListItemComposable(
            speaker = Speaker(
                districtId = "1234567",
                firstName = "Max",
                lastName = "Mustermann",
                mobile = "+49123456789",
                phone = "+497899888874",
                email = "max.mustermann@mail.com",
                spiritualStatus = SpiritualStatus.ELDER,
                congregationId = "9876543",
                active = true,
            ),
            isExpanded = isExpanded,
            onClick = { isExpanded = !isExpanded }
        )
    }
}
