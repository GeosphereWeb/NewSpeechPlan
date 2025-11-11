package de.geosphere.speechplaning.feature.login.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
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
fun ColumnScope.SignInScreen(
    onSignInClick: (String, String) -> Unit,
    onGoToSignUp: () -> Unit // Callback für den Wechsel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
        onClick = { onSignInClick(email, password) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Anmelden")
    }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onGoToSignUp) {
        Text("Noch kein Konto? Jetzt registrieren")
    }
}

@ThemePreviews
@Composable
fun SignInScreenPreview() = PreviewKoin {
    SpeechPlaningTheme {
        Column() {
            SignInScreen(
                onSignInClick = { _, _ -> },
                onGoToSignUp = {}
            )
        }
    }
}
