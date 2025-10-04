package de.geosphere.speechplaning.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import de.geosphere.speechplaning.data.AuthUiState
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

    /// --- ÄNDERUNG 1: Beide Zustände beobachten ---
    // Der globale Authentifizierungs-Status (wird vom AuthStateListener im Repo aktualisiert)
    val authUiState by authViewModel.authUiState.collectAsState()
    // Der Zustand der Login/Registrierungs-Aktion (wird direkt im ViewModel gesetzt)
    val loginActionUiState by authViewModel.loginActionUiState.collectAsState()

    // --- NEU: Effekt zum Zurücksetzen des Fehlerzustands ---
    // Wenn die Anzeige des LoginScreens verlassen wird (onDispose),
    // wird der temporäre Fehler- oder Ladezustand zurückgesetzt.
    DisposableEffect(Unit) {
        onDispose {
            authViewModel.resetActionState()
        }
    }

    // --- NEU: Context für den Toast holen ---
    val context = LocalContext.current
    // --- NEU: LaunchedEffect, der auf den authUiState reagiert ---
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Authenticated) {
            Toast.makeText(
                context,
                "Anmeldung erfolgreich!",
                Toast.LENGTH_SHORT
            ).show()
            // Hier würdest du typischerweise als Nächstes
            // zum Hauptbildschirm deiner App navigieren.
        }
    }

    // Ein Container für unsere UI-Elemente
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- E-Mail-Feld (unverändert) ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Passwort-Feld (unverändert) ---
        PasswortTextfeld(
            value = password,
            onValueChange = { password = it.trim() },
            label = "Passwort",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Buttons (unverändert) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {
                authViewModel.createUserWithEmailAndPassword(email, password)
            }) {
                Text("Registrieren")
            }

            Button(onClick = {
                authViewModel.signInWithEmailAndPassword(email, password)
            }) {
                Text("Anmelden")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {authViewModel.signOut()}) {
            Text("Logout")
        }

        // --- ÄNDERUNG 2: UI-Feedback an neue Zustände anpassen ---

        // Zeige einen Lade-Spinner, wenn eine Aktion ausgeführt wird ODER der globale Zustand noch lädt
        if (loginActionUiState.isLoading || authUiState is AuthUiState.Loading) {
            CircularProgressIndicator()
        }

        // Zeige einen Fehler an, wenn die Login/Registrierungs-Aktion fehlschlägt
        loginActionUiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Zeige eine spezifische Nachricht an, wenn der Nutzer angemeldet ist, aber noch auf Freigabe wartet.
        if (authUiState is AuthUiState.NeedsApproval) {
            Text(
                text = "Dein Konto wurde erstellt und wartet auf die Freigabe durch einen Administrator.",
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Der Erfolgsfall wird jetzt durch den globalen `authUiState` abgedeckt.
        // Wenn `authUiState` zu `Authenticated` wechselt, würdest du normalerweise
        // zu einem anderen Bildschirm navigieren. Die reine Erfolgsmeldung hier
        // ist für die weitere Entwicklung nicht mehr nötig.
        // `loginActionUiState.isSuccess` kann aber als Trigger für die Navigation dienen.

        // if (loginActionUiState.isSuccess) { ... navigiere weg ... }
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
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
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
