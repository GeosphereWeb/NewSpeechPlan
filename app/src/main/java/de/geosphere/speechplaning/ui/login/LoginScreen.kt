package de.geosphere.speechplaning.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.di.PreviewKoin
import de.geosphere.speechplaning.ui.theme.SpeechPlaningTheme
import de.geosphere.speechplaning.ui.theme.ThemePreviews
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = koinViewModel()
) {
    // Variablen für die Eingabefelder
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Wir beobachten den Zustand aus dem ViewModel
    val authUiState by authViewModel.uiState.collectAsState()

    // Ein Container für unsere UI-Elemente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- E-Mail-Feld ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Passwort-Feld ---
        PasswortTextfeld(
            value = password,
            onValueChange = { password = it.trim() },
            label = "Passwort",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Buttons für Registrierung und Anmeldung ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                // Aufruf der ViewModel-Funktion für die Registrierung
                authViewModel.createUserWithEmailAndPassword(email, password)
            }) {
                Text("Registrieren")
            }

            Button(onClick = {
                // Aufruf der ViewModel-Funktion für die Anmeldung
                authViewModel.signInWithEmailAndPassword(email, password)
            }) {
                Text("Anmelden")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- UI-Feedback basierend auf dem Zustand ---
        if (authUiState.isLoading) {
            CircularProgressIndicator()
        }

        authUiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (authUiState.success) {
            // Hier würdest du normalerweise zum nächsten Bildschirm navigieren.
            // Für den Anfang zeigen wir nur eine Erfolgsmeldung.
            Text(
                text = "Erfolgreich angemeldet/registriert!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Ein wiederverwendbares Textfeld für die Passworteingabe mit einem Button
 * zum Ein- und Ausblenden des Passworts.
 *
 * @param value Der aktuelle Wert des Textfeldes.
 * @param onValueChange Callback, der aufgerufen wird, wenn sich der Wert ändert.
 * @param label Die Beschriftung des Textfeldes.
 * @param modifier Der Modifier, der auf dieses Composable angewendet wird.
 */
@Composable
fun PasswortTextfeld(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var passwortSichtbar by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        visualTransformation = if (passwortSichtbar) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val image = if (passwortSichtbar) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwortSichtbar) "Passwort ausblenden" else "Passwort anzeigen"
            IconButton(onClick = { passwortSichtbar = !passwortSichtbar }) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )
}
// Eine Vorschau, damit du das Design in Android Studio sehen kannst
@ThemePreviews
@Composable
fun LoginScreenPreview() = PreviewKoin {
    SpeechPlaningTheme {
        LoginScreen()
    }
}
