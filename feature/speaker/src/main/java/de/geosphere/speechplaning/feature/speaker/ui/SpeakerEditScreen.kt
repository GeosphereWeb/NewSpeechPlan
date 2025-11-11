package de.geosphere.speechplaning.feature.speaker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.model.Speaker
import de.geosphere.speechplaning.core.model.data.SpiritualStatus

@Composable
fun SpeakerEditScreen(
    viewModel: SpeakerViewModel,
    speaker: Speaker?,
    onSave: () -> Unit
) {
    SpeakerEditContent(
        speaker = speaker,
        onSaveClick = {
            viewModel.saveSpeaker(it)
            onSave()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
fun SpeakerEditContent(
    speaker: Speaker?,
    onSaveClick: (Speaker) -> Unit
) {
    var nameFirst by remember { mutableStateOf(speaker?.nameFirst ?: "") }
    var nameLast by remember { mutableStateOf(speaker?.nameLast ?: "") }
    var email by remember { mutableStateOf(speaker?.email ?: "") }
    var phone by remember { mutableStateOf(speaker?.phone ?: "") }
    var mobile by remember { mutableStateOf(speaker?.mobile ?: "") }
    var isActive by remember { mutableStateOf(speaker?.isActive ?: true) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (speaker == null) "Add Speaker" else "Edit Speaker") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = nameFirst,
                onValueChange = { nameFirst = it },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nameLast,
                onValueChange = { nameLast = it },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = mobile,
                onValueChange = { mobile = it },
                label = { Text("Mobile") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                Text(text = "Active")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val updatedSpeaker = (speaker ?: Speaker()).copy(
                        nameFirst = nameFirst,
                        nameLast = nameLast,
                        email = email,
                        phone = phone,
                        mobile = mobile,
                        isActive = isActive
                        // status and speechNumberIds would be handled by a dropdown/selector
                    )
                    onSaveClick(updatedSpeaker)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(name = "Add New Speaker", showBackground = true)
@Composable
@Suppress("kotlin:S100")
private fun SpeakerEditScreenPreview_AddNew() {
    SpeakerEditContent(speaker = null, onSaveClick = {})
}

@Preview(name = "Edit Existing Speaker", showBackground = true)
@Composable
@Suppress("kotlin:S100")
private fun SpeakerEditScreenPreview_Edit() {
    val speaker = Speaker(
        id = "1",
        nameFirst = "John",
        nameLast = "Doe",
        email = "john.doe@example.com",
        phone = "123456789",
        mobile = "987654321",
        spiritualStatus = SpiritualStatus.ELDER,
        isActive = true
    )
    SpeakerEditContent(speaker = speaker, onSaveClick = {})
}
