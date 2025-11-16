package de.geosphere.speechplaning.feature.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.core.ui.atoms.di.PreviewKoin
import de.geosphere.speechplaning.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.theme.ThemePreviews

@Composable
fun ColumnScope.SignUpScreen(
    onSignUpClick: (String, String, String) -> Unit,
    onGoToSignIn: () -> Unit // Callback für den Wechsel
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Text("Neues Konto erstellen", style = MaterialTheme.typography.headlineMedium)
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it },
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("E-Mail") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
    Spacer(modifier = Modifier.height(8.dp))
    PasswordTextfieldComponent(
        value = password,
        onValueChange = { password = it.trim() },
        label = "Passwort",
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { onSignUpClick(email, password, displayName) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Registrieren")
    }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onGoToSignIn) {
        Text("Bereits ein Konto? Zur Anmeldung")
    }
}

@ThemePreviews
@Composable
fun SignUpScreenPreview() = PreviewKoin {
    SpeechPlaningTheme {
        Column {
            SignUpScreen(
                onSignUpClick = { _, _, _ -> },
                onGoToSignIn = {}
            )
        }
    }
}
